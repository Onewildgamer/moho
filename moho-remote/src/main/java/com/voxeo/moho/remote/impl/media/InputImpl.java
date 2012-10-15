/**
 * Copyright 2010-2011 Voxeo Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.voxeo.moho.remote.impl.media;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import com.rayo.core.verb.InputMode;
import com.rayo.core.verb.VerbCompleteReason;
import com.rayo.core.verb.VerbRef;
import com.voxeo.moho.MediaException;
import com.voxeo.moho.common.event.MohoInputCompleteEvent;
import com.voxeo.moho.common.util.SettableResultFuture;
import com.voxeo.moho.event.EventSource;
import com.voxeo.moho.event.InputCompleteEvent;
import com.voxeo.moho.media.Input;
import com.voxeo.moho.remote.impl.JID;
import com.voxeo.moho.remote.impl.MediaServiceSupport;
import com.voxeo.moho.remote.impl.RayoListener;
import com.voxeo.rayo.client.XmppException;
import com.voxeo.rayo.client.xmpp.stanza.IQ;
import com.voxeo.rayo.client.xmpp.stanza.Presence;

//TODO exception and IQ error handling
public class InputImpl<T extends EventSource> implements Input<T>, RayoListener {
  private static final Logger LOG = Logger.getLogger(InputImpl.class);

  protected SettableResultFuture<InputCompleteEvent<T>> _future = new SettableResultFuture<InputCompleteEvent<T>>();

  protected VerbRef _verbRef;

  protected MediaServiceSupport<T> _call;

  protected T _todo;

  public InputImpl(final VerbRef verbRef, final MediaServiceSupport<T> call, T todo) {
    _verbRef = verbRef;
    _call = call;
    _todo = todo;
    _call.addComponentListener(_verbRef.getVerbId(), this);
  }

  public void done(final InputCompleteEvent<T> event) {
    _future.setResult(event);
    _call.removeComponentListener(_verbRef.getVerbId());
  }

  public void done(final MediaException exception) {
    _future.setException(exception);
    _call.removeComponentListener(_verbRef.getVerbId());
  }

  @Override
  public void stop() {
    if (!_future.isDone()) {
      try {
        IQ iq = _call.getMohoRemote().getRayoClient().stop(_verbRef);
        if (iq.isError()) {
          com.voxeo.rayo.client.xmpp.stanza.Error error = iq.getError();
          throw new MediaException(error.getCondition() + error.getText());
        }
      }
      catch (XmppException e) {
        LOG.error("", e);
        throw new MediaException(e);
      }
    }
  }

  @Override
  public boolean cancel(final boolean mayInterruptIfRunning) {
    return _future.cancel(mayInterruptIfRunning);
  }

  @Override
  public InputCompleteEvent<T> get() throws InterruptedException, ExecutionException {
    return _future.get();
  }

  @Override
  public InputCompleteEvent<T> get(final long timeout, final TimeUnit unit) throws InterruptedException,
      ExecutionException, TimeoutException {
    return _future.get(timeout, unit);
  }

  @Override
  public boolean isCancelled() {
    return _future.isCancelled();
  }

  @Override
  public boolean isDone() {
    return _future.isDone();
  }

  @Override
  public void onRayoEvent(JID from, Presence presence) {
	  LOG.debug("InputImpl Recived presence, processing:"+ presence);
    Object obj = presence.getExtension().getObject();

    if (obj instanceof com.rayo.core.verb.VerbCompleteEvent) {
      com.rayo.core.verb.VerbCompleteEvent event = (com.rayo.core.verb.VerbCompleteEvent) obj;

      MohoInputCompleteEvent<T> mohoEvent = new MohoInputCompleteEvent<T>(_todo,
          getMohoInputCompleteReasonByRayoReason(event.getReason()), event.getErrorText(), this);
      if (event instanceof com.rayo.core.verb.InputCompleteEvent) {
        mohoEvent.setConcept(((com.rayo.core.verb.InputCompleteEvent) event).getConcept());
        mohoEvent.setConfidence(((com.rayo.core.verb.InputCompleteEvent) event).getConfidence());

        com.voxeo.moho.media.InputMode mode = null;
        if (((com.rayo.core.verb.InputCompleteEvent) event).getMode() == InputMode.DTMF) {
          mode = com.voxeo.moho.media.InputMode.DTMF;
        }
        else if (((com.rayo.core.verb.InputCompleteEvent) event).getMode() == InputMode.VOICE) {
          mode = com.voxeo.moho.media.InputMode.SPEECH;
        }
        else {
          mode = com.voxeo.moho.media.InputMode.ANY;
        }
        mohoEvent.setInputMode(mode);
        mohoEvent.setInterpretation(((com.rayo.core.verb.InputCompleteEvent) event).getInterpretation());
        mohoEvent.setNlsml(((com.rayo.core.verb.InputCompleteEvent) event).getNlsml());
        mohoEvent.setTag(((com.rayo.core.verb.InputCompleteEvent) event).getTag());
        mohoEvent.setUtterance(((com.rayo.core.verb.InputCompleteEvent) event).getUtterance());
      }

      this.done(mohoEvent);
      _call.dispatch(mohoEvent);
    }
    else {
      LOG.error("InputImpl Can't process presence:" + presence);
    }

  }

  protected InputCompleteEvent.Cause getMohoInputCompleteReasonByRayoReason(VerbCompleteReason reason) {
    InputCompleteEvent.Cause cause = InputCompleteEvent.Cause.ERROR;

    if (reason instanceof com.rayo.core.verb.InputCompleteEvent.Reason) {
      if (reason == com.rayo.core.verb.InputCompleteEvent.Reason.MATCH) {
        cause = InputCompleteEvent.Cause.MATCH;
      }
      else if (reason == com.rayo.core.verb.InputCompleteEvent.Reason.NOINPUT) {
        cause = InputCompleteEvent.Cause.INI_TIMEOUT;
      }
      else if (reason == com.rayo.core.verb.InputCompleteEvent.Reason.TIMEOUT) {
        cause = InputCompleteEvent.Cause.MAX_TIMEOUT;
      }
      else if (reason == com.rayo.core.verb.InputCompleteEvent.Reason.NOMATCH) {
        cause = InputCompleteEvent.Cause.NO_MATCH;
      }
    }
    else if (reason instanceof com.rayo.core.verb.VerbCompleteEvent.Reason) {
      if (reason == com.rayo.core.verb.VerbCompleteEvent.Reason.HANGUP) {
        return InputCompleteEvent.Cause.DISCONNECT;
      }
      else if (reason == com.rayo.core.verb.VerbCompleteEvent.Reason.STOP) {
        return InputCompleteEvent.Cause.CANCEL;
      }
      else if (reason == com.rayo.core.verb.VerbCompleteEvent.Reason.ERROR) {
        return InputCompleteEvent.Cause.ERROR;
      }
    }

    return cause;
  }

  @Override
  public void onRayoCommandResult(JID from, IQ iq) {
	  LOG.warn("Unprocessed IQ:"+iq);
  }

  public String getVerbId() {

    return _verbRef.getVerbId();
  }
}

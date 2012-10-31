/**
 * Copyright 2010 Voxeo Corporation Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.voxeo.moho.sip;

import java.util.Map;

import javax.media.mscontrol.join.Joinable.Direction;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import com.voxeo.moho.Participant.JoinType;
import com.voxeo.moho.event.JoinCompleteEvent;
import com.voxeo.moho.sip.SIPCall.State;

public class DirectNI2NOJoinDelegate extends JoinDelegate {

  protected Direction _direction;

  protected SipServletResponse _response;

  protected DirectNI2NOJoinDelegate(final SIPIncomingCall call1, final SIPOutgoingCall call2,
      final Direction direction, final SIPCallImpl peer) {
    _call1 = call1;
    _call2 = call2;
    _direction = direction;
    _peer = peer;
  }

  @Override
  public void doJoin() throws Exception {
    super.doJoin();
    ((SIPOutgoingCall) _call2).setContinueRouting(_call1);
    ((SIPOutgoingCall) _call2).call(_call1.getRemoteSdp(), _call1.getSipSession().getApplicationSession(),
        _call1.useReplacesHeader());
  }

  @Override
  protected void doInviteResponse(final SipServletResponse res, final SIPCallImpl call,
      final Map<String, String> headers) throws Exception {
    try {
      if (_call2.equals(call)) {
        if (SIPHelper.isSuccessResponse(res) || SIPHelper.isProvisionalResponse(res)) {
          _response = res;
          final SipServletResponse newRes = _call1.getSipInitnalRequest().createResponse(res.getStatus(),
              res.getReasonPhrase());
          SIPHelper.copyContent(res, newRes);
          newRes.send();
        }
        else if (SIPHelper.isErrorResponse(res)) {
          Exception ex = getExceptionByResponse(res);
          done(getJoinCompleteCauseByResponse(res), ex);
          _call2.disconnect(true, getCallCompleteCauseByResponse(res), ex, null);
        }
      }
    }
    catch (final Exception e) {
      done(JoinCompleteEvent.Cause.ERROR, e);
      _call1.fail(e);
      _call2.fail(e);
      throw e;
    }
  }

  @Override
  protected void doAck(final SipServletRequest req, final SIPCallImpl call) throws Exception {
    if (_call1.equals(call)) {
      _call1.setSIPCallState(State.ANSWERED);
      _call2.setSIPCallState(State.ANSWERED);
      try {
        final SipServletRequest ack = _response.createAck();
        SIPHelper.copyContent(req, ack);
        ack.send();
      }
      catch (final Exception e) {
        done(JoinCompleteEvent.Cause.ERROR, e);
        _call1.fail(e);
        _call2.fail(e);
        throw e;
      }
      _call1.linkCall(_call2, JoinType.DIRECT, _direction);
      _response = null;
      done(JoinCompleteEvent.Cause.JOINED, null);
    }
  }

}

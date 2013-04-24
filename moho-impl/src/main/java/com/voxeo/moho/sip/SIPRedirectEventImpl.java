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

package com.voxeo.moho.sip;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.servlet.sip.Address;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipServletResponse;

import com.voxeo.moho.Endpoint;
import com.voxeo.moho.SignalException;
import com.voxeo.moho.common.event.MohoRedirectEvent;
import com.voxeo.moho.event.EventSource;
import com.voxeo.moho.spi.ExecutionContext;

public class SIPRedirectEventImpl<T extends EventSource> extends MohoRedirectEvent<T> implements SIPRedirectEvent<T> {

  protected SipServletResponse _res;
  
  protected ExecutionContext _ctx;

  protected SIPRedirectEventImpl(final T source, final SipServletResponse res) {
    super(source);
    _res = res;
    _ctx = (ExecutionContext) source.getApplicationContext();
  }

  @Override
  public SipServletResponse getSipResponse() {
    return _res;
  }

  @Override
  public synchronized void accept(final Map<String, String> headers) throws SignalException {
    this.checkState();
    _accepted = true;
    if (this.source instanceof SIPCallImpl) {
      final SIPCallImpl call = (SIPCallImpl) this.source;
      try {
        call.doResponse(_res, headers);
      }
      catch (final Exception e) {
        throw new SignalException(e);
      }
    }
    else {
      //TODO other source such as Subscrption
    }
  }

  @Override
  public boolean isPermanent() {
    return _res.getStatus() == SipServletResponse.SC_MOVED_PERMANENTLY;
  }

  @Override
  public Endpoint getEndpoint() {
    try {
      final ListIterator<Address> headers = _res.getAddressHeaders("Contact");
      if (headers.hasNext()) {// find and return the first contact header
        Address addr = headers.next();
        SIPEndpoint ep = new SIPEndpointImpl(_ctx, addr);
        return ep;
      }
    }
    catch (final ServletParseException e) {
      throw new IllegalArgumentException(e);
    }
    return null;
  }

  @Override
  public Endpoint[] getEndpoints() {
    final List<Endpoint> retval = new ArrayList<Endpoint>();
    try {
      final ListIterator<Address> headers = _res.getAddressHeaders("Contact");
      while (headers.hasNext()) {
        Address addr = headers.next();
        SIPEndpoint ep = new SIPEndpointImpl(_ctx, addr);
        retval.add(ep);
      }
    }
    catch (final ServletParseException e) {
      throw new IllegalArgumentException(e);
    }
    return retval.toArray(new Endpoint[retval.size()]);
  }

  @Override
  public void reject(Reason reason, Map<String, String> headers) throws SignalException {
    this.checkState();
    _rejected = true;
    if (this.source instanceof SIPCallImpl) {
      final SIPCallImpl call = (SIPCallImpl) this.source;
      try {
        call.doResponse(_res, headers);
      }
      catch (final Exception e) {
        throw new SignalException(e);
      }
    }
    else {
      //TODO other source such as Subscrption
    }
  }
}

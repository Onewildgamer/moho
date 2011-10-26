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

package com.voxeo.moho.remote.impl.event;

import java.util.Map;

import com.voxeo.moho.Call;
import com.voxeo.moho.SignalException;
import com.voxeo.moho.common.event.MohoCallEvent;
import com.voxeo.moho.event.ReInviteEvent;

public class MohoReInviteEvent extends MohoCallEvent implements ReInviteEvent {
  protected boolean _accepted;

  protected boolean _rejected;

  protected boolean _isHoldRequest;

  public MohoReInviteEvent(final Call source) {
    super(source);
  }

  @Override
  public boolean isAccepted() {
    return _accepted;
  }

  @Override
  public void accept() throws SignalException {
    accept(null);
  }

  @Override
  public boolean isRejected() {
    return _rejected;
  }

  @Override
  public void reject(Reason reason) {
    reject(reason, null);
  }

  @Override
  public boolean isProcessed() {
    return isAccepted() || isRejected();
  }

  @Override
  public void accept(Map<String, String> headers) throws SignalException {
    // TODO Auto-generated method stub

  }

  @Override
  public void reject(Reason reason, Map<String, String> headers) throws SignalException {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean isHold() {
    return _isHoldRequest;
  }

  public void setHold(boolean holdRequest) {
    _isHoldRequest = holdRequest;
  }
}

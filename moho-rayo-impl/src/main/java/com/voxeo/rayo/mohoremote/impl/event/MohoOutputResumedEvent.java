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

package com.voxeo.rayo.mohoremote.impl.event;

import com.voxeo.moho.event.EventSource;
import com.voxeo.moho.event.OutputResumedEvent;

public class MohoOutputResumedEvent<T extends EventSource> extends MohoMediaNotificationEvent<T> implements OutputResumedEvent<T> {

  public MohoOutputResumedEvent(T source) {
    super(source);
  }

}
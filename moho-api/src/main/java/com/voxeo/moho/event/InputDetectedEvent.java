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

package com.voxeo.moho.event;

import java.util.Map;

import com.voxeo.moho.media.InputMode;
import com.voxeo.moho.media.input.SignalGrammar.Signal;

/**
 * If the {@link com.voxeo.moho.Call Call} is in the supervised mode,
 * this event is fired when some input -- DTMF or speech -- is detected
 * to give the application greater control of the {@link com.voxeo.moho.media.Input Input}.
 * 
 * @author wchen
 *
 */
public interface InputDetectedEvent<T extends EventSource> extends MediaNotificationEvent<T> {

  String getConcept();

  String getInterpretation();

  float getConfidence();

  String getNlsml();

  String getTag();

  InputMode getInputMode();

  /**
   * get the semantic interpretation result slots.
   * 
   * @return semantic interpretation result slots
   */
  Map<String, String> getSISlots();

  String getInput();

  boolean isStartOfSpeech();

  boolean isEndOfSpeech();

  Signal getSignal();

}

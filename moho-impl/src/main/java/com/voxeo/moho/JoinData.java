/**
 * Copyright 2010 Voxeo Corporation
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

package com.voxeo.moho;

import javax.media.mscontrol.join.Joinable.Direction;

import com.voxeo.moho.Participant.JoinType;

public class JoinData {

  protected Participant _participant;

  protected Direction _direction;

  protected JoinType _type;

  protected Participant _realJoined;

  public JoinData(final Participant p, final Direction direction, final JoinType type) {
    _participant = p;
    _direction = direction;
    _type = type;
  }

  public JoinData(final Participant p, final Direction direction, final JoinType type, Participant realJoined) {
    _participant = p;
    _direction = direction;
    _type = type;
    _realJoined = realJoined;
  }

  public Participant getParticipant() {
    return _participant;
  }

  public Direction getDirection() {
    return _direction;
  }

  public void setDirection(final Direction d) {
    _direction = d;
  }

  public JoinType getType() {
    return _type;
  }

  public void setType(final JoinType type) {
    _type = type;
  }

  public Participant getRealJoined() {
    return _realJoined;
  }

  public void setRealJoined(Participant realJoined) {
    _realJoined = realJoined;
  }
}

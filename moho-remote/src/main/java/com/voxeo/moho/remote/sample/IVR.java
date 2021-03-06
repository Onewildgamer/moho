package com.voxeo.moho.remote.sample;

import com.voxeo.moho.Call;
import com.voxeo.moho.IncomingCall;
import com.voxeo.moho.State;
import com.voxeo.moho.event.InputCompleteEvent;
import com.voxeo.moho.event.Observer;
import com.voxeo.moho.media.Output;
import com.voxeo.moho.remote.MohoRemote;
import com.voxeo.moho.remote.impl.MohoRemoteImpl;

public class IVR implements Observer {

  public static void main(String[] args) {
    MohoRemote mohoRemote = new MohoRemoteImpl();
    mohoRemote.addObserver(new IVR());
    mohoRemote.connect("usera", "1", "", "voxeo", "localhost", "localhost");
    try {
      Thread.sleep(100 * 60 * 1000);
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @State
  public void handleInvite(final IncomingCall call) throws Exception {
    call.addObserver(this);
    call.answer();
    call.setApplicationState("menu-level-1");
    call.prompt("1 for sales, 2 for support", "1,2", 0);
  }

  @State("menu-level-1")
  public void menu1(final InputCompleteEvent<Call> evt) {
    switch (evt.getCause()) {
      case MATCH:
        final Call call = evt.getSource();
        if (evt.getInterpretation().equals("1")) {
          call.setApplicationState("menu-level-2-1");
          call.prompt("1 for prism, 2 for prophecy", "1,2", 0);
        }
        else {
          call.setApplicationState("menu-level-2-2");
          call.prompt("1 for prism, 2 for prophecy", "1,2", 0);
        }
        break;
    }
  }

  @State("menu-level-2-1")
  public void menu21(final InputCompleteEvent<Call> evt) {
    switch (evt.getCause()) {
      case MATCH:
        final Call call = evt.getSource();
        if (evt.getInterpretation().equals("1")) {
          hangupAfterOutput(call, "thank you for calling prism sales");
        }
        else {
          hangupAfterOutput(call, "thank you for calling prophecy sales");
        }
        break;
    }
  }

  @State("menu-level-2-2")
  public void menu22(final InputCompleteEvent<Call> evt) {
    switch (evt.getCause()) {
      case MATCH:
        final Call call = evt.getSource();
        if (evt.getConcept().equals("1")) {
          hangupAfterOutput(call, "thank you for calling prism support");
        }
        else {
          hangupAfterOutput(call, "thank you for calling prophecy support");
        }
        break;
    }
  }

  private void hangupAfterOutput(Call call, String text) {
    Output<Call> output = call.output(text);
    try {
      if (output.get() != null) {
        call.hangup();
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
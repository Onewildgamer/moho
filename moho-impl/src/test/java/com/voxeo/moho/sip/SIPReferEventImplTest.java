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

package com.voxeo.moho.sip;

import javax.media.mscontrol.MsControlFactory;
import javax.media.mscontrol.join.Joinable.Direction;
import javax.sdp.SdpFactory;
import javax.servlet.sip.Address;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletResponse;

import junit.framework.TestCase;

import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.lib.legacy.ClassImposteriser;

import com.voxeo.moho.ApplicationContext;
import com.voxeo.moho.ApplicationContextImpl;
import com.voxeo.moho.Call;
import com.voxeo.moho.Participant.JoinType;
import com.voxeo.moho.sip.SIPIncomingCallTest.TestApp;
import com.voxeo.moho.sip.fake.MockAddress;
import com.voxeo.moho.sip.fake.MockServletContext;
import com.voxeo.moho.sip.fake.MockSipServlet;
import com.voxeo.moho.sip.fake.MockSipServletRequest;
import com.voxeo.moho.sip.fake.MockSipServletResponse;
import com.voxeo.moho.sip.fake.MockSipSession;
import com.voxeo.moho.spi.ExecutionContext;

public class SIPReferEventImplTest extends TestCase {

  Mockery mockery = new Mockery() {
    {
      setImposteriser(ClassImposteriser.INSTANCE);
    }
  };

  // JSR309 mock
  MsControlFactory msFactory = mockery.mock(MsControlFactory.class);

  // JSR289 mock
  SipServlet servlet = new MockSipServlet(mockery);

  MockSipSession session = mockery.mock(MockSipSession.class);
  
  SipApplicationSession appSession = mockery.mock(SipApplicationSession.class);

  MockSipServletRequest referReq = mockery.mock(MockSipServletRequest.class);

  MockServletContext servletContext = mockery.mock(MockServletContext.class);

  // Moho
  TestApp app = mockery.mock(TestApp.class);

  // ApplicationContextImpl is simple, no need to mock it.
  ExecutionContext appContext;

  SipFactory sipFactory;

  SdpFactory sdpFactory;

  SIPEndpoint referedEnd = mockery.mock(SIPEndpoint.class, "referedEnd");

  SIPEndpoint originEnd = mockery.mock(SIPEndpoint.class, "originEnd");

  SIPEndpoint peerEnd = mockery.mock(SIPEndpoint.class, "peerEnd");

  MockAddress referedAddr = mockery.mock(MockAddress.class, "referedAddr");

  MockAddress originAddr = mockery.mock(MockAddress.class, "originAddr");

  MockAddress peerAddr = mockery.mock(MockAddress.class, "peerAddr");
  
  MockSipServletRequest outgoingCallRequest = mockery.mock(MockSipServletRequest.class, "outgoingCallRequest");

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    appContext = new ApplicationContextImpl(app, msFactory, servlet);

    sipFactory = appContext.getSipFactory();
    sdpFactory = appContext.getSdpFactory();

    servletContext.setAttribute(ApplicationContext.APPLICATION_CONTEXT, appContext);
    session.setServletContext(servletContext);
    referReq.setSession(session);

    mockery.checking(new Expectations() {
      {
        allowing(referedEnd).getSipAddress();
        will(returnValue(referedAddr));

        allowing(originEnd).getSipAddress();
        will(returnValue(originAddr));

        allowing(peerEnd).getSipAddress();
        will(returnValue(peerAddr));

        allowing(referedAddr).clone();
        will(returnValue(referedAddr));

        allowing(referedAddr).isWildcard();
        will(returnValue(false));

        allowing(originAddr).clone();
        will(returnValue(originAddr));

        allowing(peerAddr).clone();
        will(returnValue(peerAddr));

        allowing(referReq).getAddressHeader("Refer-To");
        will(returnValue(referedAddr));

        allowing(referReq).getAddressHeader("Referred-By");
        will(returnValue(originAddr));
        
        allowing(sipFactory).createApplicationSession();
        will(returnValue(appSession));
      }
    });
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    
    appContext.destroy();
  }

  public void testAccept() {
    // prepare
    final SIPCallImpl call = mockery.mock(SIPCallImpl.class, "call");

    final SIPCallImpl peerCall = mockery.mock(SIPCallImpl.class, "peerCall");
    final MockSipSession peerSession = mockery.mock(MockSipSession.class, "peerSipSession");

    final MockSipServletResponse referResp = mockery.mock(MockSipServletResponse.class, "referResp");
    referResp.setStatus(200);

    // new call
    final MockSipSession newSession = mockery.mock(MockSipSession.class, "newSession");
    final MockSipServletRequest newInviteReq = mockery.mock(MockSipServletRequest.class, "newInvite");
    newInviteReq.setSession(newSession);

    final MockSipServletRequest notifyReq = mockery.mock(MockSipServletRequest.class, "notifyReq");

    try {
      mockery.checking(new Expectations() {
        {
          allowing(call).isAnswered();
          will(returnValue(true));

          allowing(call).getLastPeer();
          will(returnValue(peerCall));

          allowing(call).getAddress();
          will(returnValue(originEnd));

          allowing(sipFactory).createAddress(with(any(String.class)));
          will(returnValue(referedAddr));

          allowing(peerCall).getAddress();
          will(returnValue(peerEnd));

          allowing(peerCall).getSipSession();
          will(returnValue(peerSession));

          allowing(peerSession).getApplicationSession();
          will(returnValue(appSession));
          
          allowing(newInviteReq).getSession();
          will(returnValue(newSession));
          
          allowing(newSession).getApplicationSession();
          will(returnValue(appSession));
          
          allowing(sipFactory).createRequest(with(any(SipApplicationSession.class)), with(any(String.class)), with(any(Address.class)), with(any(Address.class)));
          will(returnValue(newInviteReq));

          // send response.
          oneOf(referReq).createResponse(SipServletResponse.SC_ACCEPTED);
          will(new Action() {
            @Override
            public void describeTo(final Description description) {
            }

            @Override
            public Object invoke(final Invocation invocation) throws Throwable {
              referReq.setResponse(referResp);
              referResp.setRequest(referReq);
              referResp.setSession(session);
              return referResp;
            }
          });

          oneOf(referResp).send();

          // create new call
          allowing(newSession).getRemoteParty();
          will(returnValue(referedAddr));

          allowing(newInviteReq).addHeader(with(any(String.class)), with(any(String.class)));

          oneOf(peerCall).unjoin(call);
          oneOf(peerCall).join(with(any(Call.class)), with(equal(JoinType.DIRECT)), with(equal(Direction.DUPLEX)));

          oneOf(session).createRequest("NOTIFY");
          will(returnValue(notifyReq));

          //allowing(notifyReq).addHeader(with(any(String.class)), with(any(String.class)));

          oneOf(notifyReq).setContent(with(any(String.class)), with(any(String.class)));
          oneOf(notifyReq).send();

          oneOf(session).createRequest("NOTIFY");
          will(returnValue(notifyReq));

          //allowing(notifyReq).addHeader(with(any(String.class)), with(any(String.class)));

          oneOf(notifyReq).setContent(with(any(String.class)), with(any(String.class)));
          oneOf(notifyReq).send();

          oneOf(call).disconnect();
        }
      });
    }
    catch (final Exception ex) {
      ex.printStackTrace();
    }

    // execute
    final SIPReferEventImpl referEvent = new SIPReferEventImpl(call, referReq);

    // TODO why have public void accept(final Map<String, String> headers)
    referEvent.accept(JoinType.DIRECT, Direction.DUPLEX, null);

    mockery.assertIsSatisfied();
  }
}

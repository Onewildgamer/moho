package com.voxeo.moho.common.event;

import java.util.Map;

import com.voxeo.moho.Call;
import com.voxeo.moho.event.CallCompleteEvent;

public class MohoCallCompleteEvent extends MohoCallEvent implements CallCompleteEvent {

  protected Cause _cause;

  protected Exception _exception;
  
  protected Map<String, String> _headers;

  public MohoCallCompleteEvent(final Call source, final Cause cause) {
    super(source);
    _cause = cause;
  }

  public MohoCallCompleteEvent(final Call source, final Cause cause, final Exception e) {
    super(source);
    _cause = cause;
    _exception = e;
  }


  public MohoCallCompleteEvent(final Call source, final Cause cause, final Exception e, Map<String, String> headers) {
    super(source);
    _cause = cause;
    _exception = e;
    _headers = headers;
  }
  
  public Cause getCause() {
    return _cause;
  }
  
  public Exception getException() {
	  
	  return _exception;
  }

  @Override
  public boolean isProcessed() {
    return true;
  }
  
  	@Override
	public Map<String, String> getHeaders() {

  		return _headers;
	}
  	
  @Override
  public String toString() {
    return String.format("[Event class=%s source=%s id=%s cause=%s]", getClass().getSimpleName(), source,
        hashCode(), _cause);
  }
}

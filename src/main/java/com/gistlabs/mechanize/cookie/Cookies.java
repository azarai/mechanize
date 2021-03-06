/**
 * Copyright (C) 2012 Gist Labs, LLC. (http://gistlabs.com)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.gistlabs.mechanize.cookie;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;

import com.gistlabs.mechanize.MechanizeAgent;

/**
 *  Collection of the current available cookies. 
 *  
 * @author Martin Kersten<Martin.Kersten.mk@gmail.com>
 * @version 1.0
 * @since 2012-09-12
 */
public class Cookies implements Iterable<Cookie> {
	private final MechanizeAgent agent;
	
	private final WeakHashMap<org.apache.http.cookie.Cookie, Cookie> cookieRepresentationCache = new WeakHashMap<org.apache.http.cookie.Cookie, Cookie>();
	
	public Cookies(MechanizeAgent agent) {
		this.agent = agent;
	}
	
	public MechanizeAgent getAgent() {
		return agent;
	}
	
	private Cookie getCookie(org.apache.http.cookie.Cookie cookie) {
		Cookie cookieRepresentation = cookieRepresentationCache.get(cookie);
		if(cookieRepresentation == null) {
			cookieRepresentation = new Cookie(cookie);
			cookieRepresentationCache.put(cookie, cookieRepresentation);
		}
		return cookieRepresentation;
	}
	
	/** Returns the cookie with the given name and for the given domain or null. */
	public Cookie get(String name, String domain) {
		for(Cookie cookie : this) {
			if(cookie.getName().equals(name) && cookie.getDomain().equals(domain))
				return cookie; 
		}
		return null;
	}
	
	/** Returns a list of all cookies currently managed by the underlying http client. */ 
	public List<Cookie> getAll() {
		List<Cookie> cookies = new ArrayList<Cookie>();
		for(org.apache.http.cookie.Cookie cookie : agent.getClient().getCookieStore().getCookies()) 
			cookies.add(getCookie(cookie));
		return cookies;
	}
	
	/** Returns the number of currently existing cookies. */ 
	public int getCount() {
		return agent.getClient().getCookieStore().getCookies().size();
	}
	
	public Cookie addNewCookie(String name, String value, String domain) {
		Cookie cookie = new Cookie(name, value);
		cookie.getHttpCookie().setDomain(domain);
		cookieRepresentationCache.put(cookie.getHttpCookie(), cookie);
		agent.getClient().getCookieStore().addCookie(cookie.getHttpCookie());
		return cookie;
	}

	/** Adds all cookies by actually cloning them. */
	public void addAllCloned(List<Cookie> cookies) {
		for(Cookie cookie : cookies) {
			Cookie clone = new Cookie(cookie);
			agent.getClient().getCookieStore().addCookie(clone.getHttpCookie());
		}
	}
	
	/** Removes all current managed cookies. */
	public void removeAll() {
		agent.getClient().getCookieStore().clear();
	}
	
	/** Removes the current cookie by changing the expired date and force the store to remove all expired for a given date. The date will be set to a time of 1970. */
	public void remove(Cookie cookie) {
		cookie.getHttpCookie().setExpiryDate(new Date(0));
		cookieRepresentationCache.remove(cookie.getHttpCookie());
		agent.getClient().getCookieStore().clearExpired(new Date(1));
	}
	
	public Iterator<Cookie> iterator() {
		return getAll().iterator();
	}
	
	/** Writes all cookies to System.out including all the outer HTML. */
	public void dumpAllToSystemOut() {	
		dumpToSystemOut(getAll());
	}
	
	public void dumpToSystemOut(List<Cookie> cookies) {
		for(Cookie cookie : cookies) 
			System.out.println("cookie: " + cookie);
	}
}

/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portlet.courses.dao.xml;

import java.util.Map;

import javax.portlet.PortletRequest;

import org.apache.commons.codec.binary.Base64;
import org.jasig.portlet.courses.dao.ICoursesDao;
import org.jasig.portlet.courses.model.xml.CoursesByTerm;
import org.jasig.portlet.courses.model.xml.TermList;
import org.jasig.portlet.courses.model.xml.TermsAndCourses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

/**
 * HttpClientCoursesDaoImpl retrieves courses from a Basic Authentication
 * protected XML feed.
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Revision$
 */
public class HttpClientCoursesDaoImpl implements ICoursesDao {

    private String urlFormat = "http://localhost:8180/jasig-courses-integration/course-summary";

    public void setUrlFormat(String urlFormat) {
        this.urlFormat = urlFormat;
    }

    private String usernameKey = "user.login.id";
    
    public void setUsernameKey(String usernameKey) {
        this.usernameKey = usernameKey;
    }
    
    private String passwordKey = "password";
    
    public void setPasswordKey(String passwordKey) {
        this.passwordKey = passwordKey;
    }
    
    private RestTemplate restTemplate;

    @Autowired(required = true)
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public TermList getTermList(PortletRequest request) {
        final TermsAndCourses termsAndCourses = this.getTermsAndCourses(request);
        return termsAndCourses.getTermList();
    }

    @Override
    public CoursesByTerm getCoursesByTerm(PortletRequest request, String termCode) {
        final TermsAndCourses termsAndCourses = this.getTermsAndCourses(request);
        return termsAndCourses.getCoursesByTerm(termCode);
    }
    
    /**
     * Get terms and courses for the current user  
     */
    protected TermsAndCourses getTermsAndCourses(PortletRequest request) {
        HttpEntity<?> requestEntity = getRequestEntity(request);

        HttpEntity<TermsAndCourses> response = restTemplate.exchange(
                urlFormat, HttpMethod.GET, requestEntity,
                TermsAndCourses.class);
        
        return response.getBody();
    }
    
    /**
     * Get a request entity prepared for basic authentication.
     */
    protected HttpEntity<?> getRequestEntity(PortletRequest request) {
        @SuppressWarnings("unchecked")
        Map<String,String> userInfo = (Map<String,String>) request.getAttribute(PortletRequest.USER_INFO);
        String username = userInfo.get(this.usernameKey);
        String password = userInfo.get(this.passwordKey);
        
        HttpHeaders requestHeaders = new HttpHeaders();
        String authString = username.concat(":").concat(password);
        String encodedAuthString = new Base64().encodeToString(authString.getBytes());
        requestHeaders.set("Authorization", "Basic ".concat(encodedAuthString));
        HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);
        return requestEntity;
    }

}

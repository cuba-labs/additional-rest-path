package com.company.demo.restapi;

import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.core.global.MessageTools;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.SecurityContext;
import com.haulmont.cuba.security.app.TrustedClientService;
import com.haulmont.cuba.security.global.LoginException;
import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.cuba.web.auth.WebAuthConfig;
import com.haulmont.restapi.auth.OAuthTokenIssuer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import java.util.Collections;

@Controller
public class DemoAuthController {
    @Inject
    private OAuthTokenIssuer oAuthTokenIssuer;
    @Inject
    private Configuration configuration;
    @Inject
    private TrustedClientService trustedClientService;
    @Inject
    private MessageTools messageTools;

    @RequestMapping(method = RequestMethod.POST, path = "login")
    public ResponseEntity post() {
        // obtain system session to be able to call middleware services
        WebAuthConfig webAuthConfig = configuration.getConfig(WebAuthConfig.class);
        UserSession systemSession;
        try {
            systemSession = trustedClientService.getSystemSession(webAuthConfig.getTrustedClientPassword());
        } catch (LoginException e) {
            throw new RuntimeException("Error during system auth");
        }

        // set security context
        AppContext.setSecurityContext(new SecurityContext(systemSession));
        try {
            // generate token for "promo-user"
            OAuthTokenIssuer.OAuth2AccessTokenResult tokenResult =
                    oAuthTokenIssuer.issueToken("admin", messageTools.getDefaultLocale(), Collections.emptyMap());
            OAuth2AccessToken accessToken = tokenResult.getAccessToken();

            // set security HTTP headers to prevent browser caching of security token
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CACHE_CONTROL, "no-store");
            headers.set(HttpHeaders.PRAGMA, "no-cache");
            return new ResponseEntity<>(accessToken, headers, HttpStatus.OK);
        } finally {
            // clean up security context
            AppContext.setSecurityContext(null);
        }
    }
}
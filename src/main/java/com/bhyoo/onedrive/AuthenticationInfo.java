package com.bhyoo.onedrive;

import lombok.Getter;
import lombok.Setter;

import static lombok.AccessLevel.PRIVATE;

// TODO: Enhance javadoc

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class AuthenticationInfo {
	@Getter(PRIVATE) @Setter(PRIVATE) private String token_type;
	@Getter(PRIVATE) private long expires_in;
	@Getter(PRIVATE) @Setter(PRIVATE) private String access_token;
	@Getter(PRIVATE) @Setter(PRIVATE) private String refresh_token;
	@Getter(PRIVATE) @Setter(PRIVATE) private String user_id;

	private void setExpires_in(long expires_in) {
		this.expires_in = expires_in * 1000 + System.currentTimeMillis();
	}

	public String tokenType() {return token_type;}

	public String accessToken() {return access_token;}

	public String refreshToken() {return refresh_token;}

	public String userId() {return user_id;}

	public long expiresIn() {return expires_in;}
}

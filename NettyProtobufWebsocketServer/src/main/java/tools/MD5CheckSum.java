package tools;

import java.security.MessageDigest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * md5校验
 * @author Administrator
 *
 */
public class MD5CheckSum {
	private static final Logger log = LoggerFactory.getLogger(MD5CheckSum.class);

	public byte[] checksum(byte[] bytes) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.update(bytes);
			return messageDigest.digest();
		} catch (Exception e) {
			log.error("unsupported digest: md5", e);
			return new byte[0];
		}
	}

	public int length() {
		return 8;
	}
}

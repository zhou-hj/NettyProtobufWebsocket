package tools;

/**
 * CRC16 循环冗余校验
 * @author Administrator
 *
 */
public class CRC16CheckSum {
	
	public byte[] checksum(byte[] bytes) {
		int crc = 0xffff;
		for (int i = 0; i < bytes.length; i++) {
			if (bytes[i] < 0) {
				crc ^= (int) bytes[i] + 256;
			} else {
				crc ^= (int) bytes[i];
			}
			for (int j = 0; j < 8; j++) {
				if ((crc & 0x0001) != 0) {
					crc >>= 1;
					crc ^= 0xa001;
				} else {
					crc >>= 1;
				}
			}
		}

		byte[] result = new byte[2];
		result[0] = (byte)((crc >> 8) & 0xff);
		result[1] = (byte) (crc & 0xff);
		return result;
	}

	
	public int length() {
		return 2;
	}
}

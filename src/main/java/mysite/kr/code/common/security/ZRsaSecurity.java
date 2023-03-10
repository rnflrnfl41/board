package mysite.kr.code.common.security;

import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *  TODO 
 *  @author 
 */
public class ZRsaSecurity {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final int KEY_SIZE = 1024;
	private final String Alg_NM="RSA";

	private PublicKey publicKey;
	private PrivateKey privateKey;
	private KeyFactory keyFactory =null;
	private RSAPublicKeySpec  rsaPksc= null;
	
	public ZRsaSecurity() throws Exception {
		//어떤 암호화 방식을 할것인지 세팅해줌
		KeyPairGenerator generator = KeyPairGenerator.getInstance(Alg_NM);
		generator.initialize(KEY_SIZE);
		KeyPair keyPair = generator.genKeyPair();
		//RSA방식으로 keyFactory를 만들어라
		keyFactory = KeyFactory.getInstance(Alg_NM);
		this.publicKey = keyPair.getPublic();
		this.privateKey = keyPair.getPrivate();
		this.rsaPksc= (RSAPublicKeySpec) keyFactory.getKeySpec(publicKey, RSAPublicKeySpec.class);
	}
	

	/**
	 * 
	 *  공개키를 문자열로 변환
	 *  @author 
	 *  @date   
	 *  
	 *  @return
	 */
	public String getRsaPublicKeyModulus() throws Exception{
		if(null != rsaPksc){
			return rsaPksc.getModulus().toString(16);
		}else{
			throw new Exception();
		}
	}
	
	/**
	 * 
	 *  공개키를 문자열로 변환
	 *  @author 
	 *  @date 
	 *  
	 *  @return
	 */
	public String getRsaPublicKeyExponent() throws Exception{
		if(null != rsaPksc){
			return rsaPksc.getPublicExponent().toString(16);
		}else{
			throw new Exception();
		}
	}

	/**
	 * @return the publicKey
	 */
	public PublicKey getPublicKey() {
		return this.publicKey;
	}

	/**
	 * @return the privateKey
	 */
	public PrivateKey getPrivateKey() {
		return this.privateKey;
	}

	public String encryptRSA(PublicKey publicKey, String input){
		String s = "";
		try{
			// Cipher는 암호화 해주는 모듈
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			
			//평문을 utf-8로 인코딩 해준 후 
			//바이트형식으로 잘라서 공개키로 암호화 해준 뒤 ba배열에 담음
			byte[] ba = cipher.doFinal(input.getBytes(Charset.forName("utf-8")));
			
			//바이트형식으로 배열에 담겨 있는 암호문을 문자열 타입으로 변환
			StringBuilder sb = new StringBuilder();
		    for (byte b : ba) {
		    	//앞의 빈자리를 0으로 채운뒤 16진수로 변환
		        sb.append(String.format("%02X", b));
		    }
		    s = sb.toString();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			
		}
		
		return s;
	}
	
	
	public String decryptRSA(PrivateKey privateKey, String securedValue) {
		String decryptedValue="";
		try{
			logger.info("will decrypt : " + securedValue);
			Cipher cipher = Cipher.getInstance("RSA");
			byte[] encryptedBytes = hexToByteArray(securedValue);
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
			decryptedValue = new String(decryptedBytes, "utf-8");
		}catch(Exception e){
			decryptedValue="";
		}
		
		return decryptedValue;
	}

	/**
	 * 16진 문자열을 byte 배열로 변환한다.
	 */
	public byte[] hexToByteArray(String hex) {
		if (hex == null || hex.length() % 2 != 0) {
			return new byte[] {};
		}

		byte[] bytes = new byte[hex.length() / 2];
		for (int i = 0; i < hex.length(); i += 2) {
			byte value = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
			bytes[(int) Math.floor(i / 2)] = value;
		}
		return bytes;
	}

}

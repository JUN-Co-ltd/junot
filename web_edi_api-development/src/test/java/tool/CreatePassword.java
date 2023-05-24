package tool;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * パスワード生成.
 */
public class CreatePassword {
    /**
     * パスワード生成 メイン処理.
     * @param args 引数配列
     */
    public static void main(final String[] args) {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        for(String inputPassword:args) {
            String password = encoder.encode(inputPassword);
            System.out.println(inputPassword+"："+password);
        }


    }
}

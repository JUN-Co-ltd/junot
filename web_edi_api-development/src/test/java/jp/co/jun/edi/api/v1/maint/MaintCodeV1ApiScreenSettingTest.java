package jp.co.jun.edi.api.v1.maint;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigInteger;

import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;


import jp.co.jun.edi.entity.MUserEntity;
import test.utils.LoginUserUtils;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class MaintCodeV1ApiScreenSettingTest {
    // モックサーバー
    @Autowired
    private MockMvc mockServer;
    /**
     *
     *
     */
    @Test
    public void testSuccessGetScreenSetting() throws Exception {
        final MUserEntity user = createUser();

        final MvcResult actual = mockServer
                .perform(MockMvcRequestBuilders.get("/api/v1/maint/maint-code/05/screenSettings")
                        .with(user(LoginUserUtils.generateLoginUser(user)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON) // ContentType設定
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()) //HTTPレスポンスコード：200
                .andReturn();

        System.out.println( actual);

    }

    private MUserEntity createUser() {
        MUserEntity user = new MUserEntity();
        user.setId(BigInteger.valueOf(1));
        user.setAccountName("000002");
        user.setCompany("000000");
        user.setPassword("123456");
        user.setAuthority("ROLE_USER,ROLE_JUN,ROLE_ADMIN");
        return user;
    }
}

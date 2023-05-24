package jp.co.jun.edi.api.v1;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigInteger;

import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
public class FukukitaruItemV1ApiGetTest {
    // モックサーバー
    @Autowired
    private MockMvc mockServer;

    @Test
    //@WithMockUser(username = "581209\n000001", password = "H3RYB9F8", roles = { "USER", "JUN" })
    public void test_get_0001() throws Exception {
        final MUserEntity user = createUser();

        final MvcResult actual = mockServer
                .perform(MockMvcRequestBuilders.get("/api/v1/fukukitaru/items/4")
                        .with(user(LoginUserUtils.generateLoginUser(user))))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()) //HTTPレスポンスコード：200
                .andReturn();
        System.out.println(actual.getResponse().getContentAsString());
    }


    private MUserEntity createUser() {
        MUserEntity user = new MUserEntity();
        user.setId(BigInteger.valueOf(1));
        user.setAccountName("000001");
        user.setCompany("000000");
        user.setPassword("H3RYB9F8");
        user.setAuthority("ROLE_JUN,ROLE_USER");
        return user;
    }
}

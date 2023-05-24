package jp.co.jun.edi.api.v1.maint;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigInteger;
import java.util.LinkedHashMap;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.co.jun.edi.entity.MUserEntity;
import jp.co.jun.edi.model.maint.code.MaintCodeSearchConditionModel;
import test.utils.LoginUserUtils;

/**
 * 検索条件APIテスト.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class MaintCodeV1ApiSearchTest {
    // モックサーバー
    @Autowired
    private MockMvc mockServer;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void 絞り込み条件なしの初回ページ情報取得() throws Exception {
        final MUserEntity user = createUser();

        // リクエストパラメータ
        final MaintCodeSearchConditionModel test = new MaintCodeSearchConditionModel() {
            private static final long serialVersionUID = 1L;
            {
                // 絞り込み条件 なし
                setConditions(new LinkedHashMap<String, String>());
                // 1ページの最大件数 100
                setMaxResults(100);
                // 表示ページ
                setPage(0);
            }
        };
        final MvcResult actual = mockServer
                .perform(MockMvcRequestBuilders.post("/api/v1/maint/maint-code/05/search")
                        .with(user(LoginUserUtils.generateLoginUser(user)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON) // ContentType設定
                        .content(objectMapper.writeValueAsString(test)) //JSON設定
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()) //HTTPレスポンスコード：200
                .andReturn();

        System.out.println( actual);

    }

    @Test
    public void 絞り込み条件あり_1項目1単語_初回ページ情報取得() throws Exception {
        final MUserEntity user = createUser();

        // リクエストパラメータ
        final MaintCodeSearchConditionModel test = new MaintCodeSearchConditionModel() {
            private static final long serialVersionUID = 1L;
            {
                // 絞り込み条件 なし
                setConditions(new LinkedHashMap<String, String>(){
                    private static final long serialVersionUID = 1L;

                    {
                        put("item2", "ス");
                    }
                });
                // 1ページの最大件数 100
                setMaxResults(100);
                // 表示ページ
                setPage(0);
            }
        };
        final MvcResult actual = mockServer
                .perform(MockMvcRequestBuilders.post("/api/v1/maint/maint-code/05/search")
                        .with(user(LoginUserUtils.generateLoginUser(user)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON) // ContentType設定
                        .content(objectMapper.writeValueAsString(test)) //JSON設定
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()) //HTTPレスポンスコード：200
                .andReturn();

        System.out.println( actual);

    }

    @Test
    public void 絞り込み条件あり_1項目2単語_初回ページ情報取得() throws Exception {
        final MUserEntity user = createUser();

        // リクエストパラメータ
        final MaintCodeSearchConditionModel test = new MaintCodeSearchConditionModel() {
            private static final long serialVersionUID = 1L;
            {
                // 絞り込み条件 なし
                setConditions(new LinkedHashMap<String, String>(){
                    private static final long serialVersionUID = 1L;

                    {
                        put("item2", "ス ン");
                    }
                });
                // 1ページの最大件数 100
                setMaxResults(100);
                // 表示ページ
                setPage(0);
            }
        };
        final MvcResult actual = mockServer
                .perform(MockMvcRequestBuilders.post("/api/v1/maint/maint-code/05/search")
                        .with(user(LoginUserUtils.generateLoginUser(user)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON) // ContentType設定
                        .content(objectMapper.writeValueAsString(test)) //JSON設定
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()) //HTTPレスポンスコード：200
                .andReturn();

        System.out.println( actual);

    }

    @Test
    public void 絞り込み条件あり_2項目2単語_初回ページ情報取得() throws Exception {
        final MUserEntity user = createUser();

        // リクエストパラメータ
        final MaintCodeSearchConditionModel test = new MaintCodeSearchConditionModel() {
            private static final long serialVersionUID = 1L;
            {
                // 絞り込み条件 なし
                setConditions(new LinkedHashMap<String, String>(){
                    private static final long serialVersionUID = 1L;

                    {
                        put("item2", "ス ン");
                        put("item1", "ラ カ");
                    }
                });
                // 1ページの最大件数 100
                setMaxResults(100);
                // 表示ページ
                setPage(0);
            }
        };
        final MvcResult actual = mockServer
                .perform(MockMvcRequestBuilders.post("/api/v1/maint/maint-code/05/search")
                        .with(user(LoginUserUtils.generateLoginUser(user)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON) // ContentType設定
                        .content(objectMapper.writeValueAsString(test)) //JSON設定
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

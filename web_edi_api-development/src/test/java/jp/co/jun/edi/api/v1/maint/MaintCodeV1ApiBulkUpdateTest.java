package jp.co.jun.edi.api.v1.maint;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
import jp.co.jun.edi.model.maint.code.MaintCodeBulkUpdateModel;
import jp.co.jun.edi.util.DateUtils;
import test.utils.LoginUserUtils;

/**
 * 登録・更新条件APIテスト.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class MaintCodeV1ApiBulkUpdateTest {
    // モックサーバー
    @Autowired
    private MockMvc mockServer;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void 登録1件() throws Exception {
        final MUserEntity user = createUser();

        // リクエストパラメータ
        final MaintCodeBulkUpdateModel test = new MaintCodeBulkUpdateModel() {
            private static final long serialVersionUID = 1L;
            {
                // 絞り込み条件 なし
                setRevisionedAt(DateUtils.stringToDate("2020/02/27"));
                setItems(new ArrayList<Map<String, String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        add(new HashMap<String, String>() {
                            private static final long serialVersionUID = 1L;
                            {
                                put("id", null);
                                put("tblid", "05");
                                put("code1", "003");
                                put("item1", "テスト用item1");
                                put("item2", "テスト用item2");
                                put("item3", "テスト用item3");
                                put("item4", "1");
                                put("mntflg", "1");
                                put("created_at", "2020-01-01'T'000000.000Z");
                                put("created_user_id", "1");
                                put("updated_at", "2020-01-01'T'000000.000Z");
                                put("updated_user_id", "1");
                                put("deleted_at", "");
                            }
                        });

                    }
                });
            }
        };
        final MvcResult actual = mockServer.perform(MockMvcRequestBuilders.post("/api/v1/maint/maint-code/05/bulkUpdate")
                .with(user(LoginUserUtils.generateLoginUser(user))).with(csrf()).contentType(MediaType.APPLICATION_JSON) // ContentType設定
                .content(objectMapper.writeValueAsString(test)) // JSON設定
        ).andDo(MockMvcResultHandlers.print()).andExpect(status().isOk()) // HTTPレスポンスコード：200
                .andReturn();

        System.out.println(actual);

    }
    @Test
    public void 更新1件() throws Exception {
        final MUserEntity user = createUser();

        // リクエストパラメータ
        final MaintCodeBulkUpdateModel test = new MaintCodeBulkUpdateModel() {
            private static final long serialVersionUID = 1L;
            {
                // 絞り込み条件 なし
                setRevisionedAt(DateUtils.stringToDate("2020/02/27"));
                setItems(new ArrayList<Map<String, String>>() {
                    private static final long serialVersionUID = 1L;
                    {
                        add(new HashMap<String, String>() {
                            private static final long serialVersionUID = 1L;
                            {
                                put("id", "89514");
                                put("tblid", "05");
                                put("code1", "003");
                                put("item1", "テスト用item1更新");
                                put("item2", "テスト用item2更新");
                                put("item3", "テスト用item3更新");
                                put("item4", "1");
                                put("mntflg", "1");
                                put("created_at", "2020-01-01'T'000000.000Z");
                                put("created_user_id", "1");
                                put("updated_at", "2020-01-01'T'000000.000Z");
                                put("updated_user_id", "1");
                                put("deleted_at", "");
                            }
                        });

                    }
                });
            }
        };
        final MvcResult actual = mockServer.perform(MockMvcRequestBuilders.post("/api/v1/maint/maint-code/05/bulkUpdate")
                .with(user(LoginUserUtils.generateLoginUser(user))).with(csrf()).contentType(MediaType.APPLICATION_JSON) // ContentType設定
                .content(objectMapper.writeValueAsString(test)) // JSON設定
        ).andDo(MockMvcResultHandlers.print()).andExpect(status().isOk()) // HTTPレスポンスコード：200
                .andReturn();

        System.out.println(actual);

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

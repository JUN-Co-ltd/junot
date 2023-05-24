// CHECKSTYLE:OFF

package jp.co.jun.edi.api.v1;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigInteger;

import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import jp.co.jun.edi.service.parameter.GetServiceParameter;

/**
 * MisleadingRepresentationApiテスト.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class MisleadingRepresentationV1ApiGetTest {
    // モックサーバー
    @Autowired
    private MockMvc mockServer;

//    @Autowired
//    private ObjectMapper objectMapper;

//    @MockBean
//    private MisleadingRepresentationGetService misRepGetService;

    // モックオブジェクトの挿入対象
    @InjectMocks
    private MisleadingRepresentationFileV1Api misRepApi;

    // モックへの引数を取得するためのキャプター
    @Captor
    ArgumentCaptor<GetServiceParameter<BigInteger>> idCaptor;

    /**
     * 観点1.正しい優良誤認検査IDで検索し、Serviceに正しい値を渡せていることを確認する.
     * 観点2.正しい優良誤認検査IDで検索し、APIが正しい値を返せていることを確認する(OUT).
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "000000\n000001", password = "H3RYB9F8", roles = { "USER", "JUN" })
    public void test_get_success() throws Exception {
//        final MisleadingRepresentationModel tes = new MisleadingRepresentationModel();
//        //渡された優良誤認検査ID(1)を設定
//        tes.setId(BigInteger.valueOf(1));
//        //Serviceの戻り値(Service自体は実行されていない)
//        final GetServiceResponse<MisleadingRepresentationModel> res = GetServiceResponse.<MisleadingRepresentationModel>builder().item(tes).build();
//
////        //モックしたサービスの挙動を設定する：どんな引数の場合でもresを返す
////        doReturn(res).when(misRepGetService).call(ArgumentMatchers.<GetServiceParameter<BigInteger>>any());
//
//        //APIを叩く
//        final MvcResult actual = mockServer.perform(MockMvcRequestBuilders.get("/api/v1/misleadingRepresentations/1"))
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(status().isOk()) //HTTPレスポンスコード：200
//                .andReturn();
//
//        //観点1.
//        //モックしたインスタンス内の関数 (call)が呼び出されていることを確認(正しいServiceを呼び出しているか)
////        verify(misRepGetService, times(1)).call(idCaptor.capture());
//        //関数に渡しているパラメーターが自分で指定したもの(1)であることを確認
//        assertThat(idCaptor.getValue().getId()).isEqualTo(1);
//
//        //観点2.
//        //APIが返す想定データ
//        final String expected = objectMapper.writeValueAsString(res.getItem());
//        //APIが正しい結果を返していることを確認
//        assertThat(actual.getResponse().getContentAsString()).isEqualTo(expected);

    }

    /**
     * 観点3.優良誤認検査IDがnullの場合、validationではじかれていることを確認する(IN).
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "000000\n000001", password = "H3RYB9F8", roles = { "USER", "JUN" })
    public void test_get_error_405() throws Exception {

        //APIを叩く
        final MvcResult actual = mockServer.perform(MockMvcRequestBuilders.get("/api/v1/misleadingRepresentations/"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isMethodNotAllowed()) //HTTPレスポンスコード：405
                .andReturn();

        //APIが返す中身は空ということを確認
        assertThat(actual.getResponse().getContentAsString()).isEqualTo("");

    }

    /**
     * 観点3.優良誤認検査IDが非intの場合、validationではじかれていることを確認する(IN).
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "000000\n000001", password = "H3RYB9F8", roles = { "USER", "JUN" })
    public void test_get_error_400() throws Exception {

        //APIを叩く
        final MvcResult actual = mockServer.perform(MockMvcRequestBuilders.get("/api/v1/misleadingRepresentations/あ"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest()) //HTTPレスポンスコード：400
                .andReturn();

        //APIが返す中身は空ということを確認
        assertThat(actual.getResponse().getContentAsString()).isEqualTo("");

    }


}
//CHECKSTYLE:ON

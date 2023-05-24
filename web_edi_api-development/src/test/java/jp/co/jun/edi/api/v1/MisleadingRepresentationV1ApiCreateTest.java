// CHECKSTYLE:OFF

package jp.co.jun.edi.api.v1;

import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

import jp.co.jun.edi.service.MisleadingRepresentationFileCreateService;

/**
 * MisleadingRepresentationApiテスト.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class MisleadingRepresentationV1ApiCreateTest {
//    // モックサーバー
//    @Autowired
//    private MockMvc mockServer;
//
//    @Autowired
//    private ObjectMapper objectMapper;

    @MockBean
    private MisleadingRepresentationFileCreateService misRepCreateService;

    // モックオブジェクトの挿入対象
    @InjectMocks
    private MisleadingRepresentationFileV1Api misRepApi;

//    // モックへの引数を取得するためのキャプター
//    @Captor
//    ArgumentCaptor<CreateServiceParameter<MisleadingRepresentationModel>> modelCaptor;

    /**
     * 観点1.正しい優良誤認検査IDで検索し、Serviceに正しい値を渡せていることを確認する.
     * 観点2.正しい優良誤認検査IDで検索し、APIが正しい値を返せていることを確認する(OUT).
     * 観点3.情報がすべてある場合(IN).
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "000000\n000001", password = "H3RYB9F8", roles = { "USER", "JUN" })
    public void test_create_success() throws Exception {
//        final MisleadingRepresentationModel tes = new MisleadingRepresentationModel();
//
//        tes.setId(BigInteger.valueOf(1));
//
//        final ItemModel itemModel = new ItemModel();
//        itemModel.setId(BigInteger.valueOf(1));
//        final List<ItemModel> itemList = Arrays.asList(itemModel);
//        tes.setItems(itemList);
//
//        final MisleadingRepresentationFileModel misRepFileModel = new MisleadingRepresentationFileModel();
//        misRepFileModel.setId(BigInteger.valueOf(1));
//        final List<MisleadingRepresentationFileModel> misRepFileList = Arrays.asList(misRepFileModel);
//        tes.setMisleadingRepresentationFiles(misRepFileList);
//
//        //Serviceの戻り値(Service自体は実行されていない)
//        final CreateServiceResponse<MisleadingRepresentationModel> res =CreateServiceResponse.<MisleadingRepresentationModel>builder().item(tes).build();
//
//        //モックしたサービスの挙動を設定する：どんな引数の場合でもresを返す
////        doReturn(res).when(misRepCreateService).call(ArgumentMatchers.<CreateServiceParameter<MisleadingRepresentationModel>>any());
//
//        tes.setId(null);
//
//        //APIを叩く
//        final MvcResult actual = mockServer.perform(MockMvcRequestBuilders.post("/api/v1/misleadingRepresentations")
//                .with(csrf())
//                // ContentTypeの設定
//                .contentType(MediaType.APPLICATION_JSON)
//                // Jsonの設定
//                .content(objectMapper.writeValueAsString(tes)))
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(status().isOk()) //HTTPレスポンスコード：200
//                .andReturn();
//
//        //観点1.
//        //モックしたインスタンス内の関数 (call)が呼び出されていることを確認(正しいServiceを呼び出しているか)
////        verify(misRepCreateService, times(1)).call(modelCaptor.capture());
//        //関数に渡している優良誤認idはnullであることを確認
//        assertThat(modelCaptor.getValue().getItem().getId()).isNull();
//
//        //観点2.
//        //APIが返す想定データ
//        final String expected = objectMapper.writeValueAsString(res.getItem());
//        //APIが正しい結果を返していることを確認
//        assertThat(actual.getResponse().getContentAsString()).isEqualTo(expected);

    }

    /**
     * 観点3.優良誤認ファイル情報リストがない場合(IN).
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "000000\n000001", password = "H3RYB9F8", roles = { "USER", "JUN" })
    public void test_create_success_noMisRepFiles() throws Exception {
//        final MisleadingRepresentationModel tes = new MisleadingRepresentationModel();
//
//        tes.setId(BigInteger.valueOf(1));
//
//        final MisleadingRepresentationFileModel misRepFileModel = new MisleadingRepresentationFileModel();
//        misRepFileModel.setId(BigInteger.valueOf(1));
//        final List<MisleadingRepresentationFileModel> misRepFileList = Arrays.asList(misRepFileModel);
//        tes.setMisleadingRepresentationFiles(misRepFileList);
//
//        final CreateServiceResponse<MisleadingRepresentationModel> res =CreateServiceResponse.<MisleadingRepresentationModel>builder().item(tes).build();
//
////        doReturn(res).when(misRepCreateService).call(ArgumentMatchers.<CreateServiceParameter<MisleadingRepresentationModel>>any());
//
//        tes.setId(null);
//
//        //APIを叩く
//        final MvcResult actual = mockServer.perform(MockMvcRequestBuilders.post("/api/v1/misleadingRepresentations")
//                .with(csrf())
//                // ContentTypeの設定
//                .contentType(MediaType.APPLICATION_JSON)
//                // Jsonの設定
//                .content(objectMapper.writeValueAsString(tes)))
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(status().isOk()) //HTTPレスポンスコード：200
//                .andReturn();
//
//        //観点1.
//        //モックしたインスタンス内の関数 (call)が呼び出されていることを確認(正しいServiceを呼び出しているか)
////        verify(misRepCreateService, times(1)).call(modelCaptor.capture());
//        //関数に渡している優良誤認idはnullであることを確認
//        assertThat(modelCaptor.getValue().getItem().getId()).isNull();
//
//        //観点2.
//        //APIが返す想定データ
//        final String expected = objectMapper.writeValueAsString(res.getItem());
//        //APIが正しい結果を返していることを確認
//        assertThat(actual.getResponse().getContentAsString()).isEqualTo(expected);

    }

    /**
     * 観点3.品番リストがない場合(IN).
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "000000\n000001", password = "H3RYB9F8", roles = { "USER", "JUN" })
    public void test_create_success_noItems() throws Exception {
//        final MisleadingRepresentationModel tes = new MisleadingRepresentationModel();
//
//        tes.setId(BigInteger.valueOf(1));
//
//        final ItemModel itemModel = new ItemModel();
//        itemModel.setId(BigInteger.valueOf(1));
//        final List<ItemModel> itemList = Arrays.asList(itemModel);
//        tes.setItems(itemList);
//
//        final CreateServiceResponse<MisleadingRepresentationModel> res =CreateServiceResponse.<MisleadingRepresentationModel>builder().item(tes).build();
//
////        doReturn(res).when(misRepCreateService).call(ArgumentMatchers.<CreateServiceParameter<MisleadingRepresentationModel>>any());
//
//        tes.setId(null);
//
//        //APIを叩く
//        final MvcResult actual = mockServer.perform(MockMvcRequestBuilders.post("/api/v1/misleadingRepresentations")
//                .with(csrf())
//                // ContentTypeの設定
//                .contentType(MediaType.APPLICATION_JSON)
//                // Jsonの設定
//                .content(objectMapper.writeValueAsString(tes)))
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(status().isOk()) //HTTPレスポンスコード：200
//                .andReturn();
//
//        //観点1.
//        //モックしたインスタンス内の関数 (call)が呼び出されていることを確認(正しいServiceを呼び出しているか)
////        verify(misRepCreateService, times(1)).call(modelCaptor.capture());
//        //関数に渡している優良誤認idはnullであることを確認
//        assertThat(modelCaptor.getValue().getItem().getId()).isNull();
//
//        //観点2.
//        //APIが返す想定データ
//        final String expected = objectMapper.writeValueAsString(res.getItem());
//        //APIが正しい結果を返していることを確認
//        assertThat(actual.getResponse().getContentAsString()).isEqualTo(expected);

    }

    /**
     * 観点3.情報がすべてない場合(IN).
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "000000\n000001", password = "H3RYB9F8", roles = { "USER", "JUN" })
    public void test_create_success_null() throws Exception {
//        final MisleadingRepresentationModel tes = new MisleadingRepresentationModel();
//
//        tes.setId(BigInteger.valueOf(1));
//
//        final CreateServiceResponse<MisleadingRepresentationModel> res =CreateServiceResponse.<MisleadingRepresentationModel>builder().item(tes).build();
//
//        tes.setId(null);
//
////        doReturn(res).when(misRepCreateService).call(ArgumentMatchers.<CreateServiceParameter<MisleadingRepresentationModel>>any());
//
//      //APIを叩く
//        final MvcResult actual = mockServer.perform(MockMvcRequestBuilders.post("/api/v1/misleadingRepresentations")
//                .with(csrf())
//                // ContentTypeの設定
//                .contentType(MediaType.APPLICATION_JSON)
//                // Jsonの設定
//                .content(objectMapper.writeValueAsString(tes)))
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(status().isOk()) //HTTPレスポンスコード：200
//                .andReturn();
//
//        //APIが返す想定データ
//        final String expected = objectMapper.writeValueAsString(res.getItem());
//        //APIが正しい結果を返していることを確認
//        assertThat(actual.getResponse().getContentAsString()).isEqualTo(expected);

    }



}
//CHECKSTYLE:ON

// CHECKSTYLE:OFF

package jp.co.jun.edi.service;

import java.math.BigInteger;

import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.co.jun.edi.repository.extended.ExtendedTItemRepository;

/**
 * MisleadingRepresentationGetServiceテスト.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
public class MisleadingRepresentationGetServiceTest {

//    @Mock
//    private TMisleadingRepresentationRepository tMisRepRepo;
//    @Mock
//    private ExtendedTMisleadingRepresentationFileRepository exTMisRepFileRepo;
    @Mock
    private ExtendedTItemRepository exTItemRepo;

    //一部だけをMock化
    @Spy
    private ObjectMapper objectMapper;

//    // モックオブジェクトの挿入対象
//    @InjectMocks
//    private MisleadingRepresentationGetService misRepGetService;

    // モックへの引数を取得するためのキャプター
    @Captor
    ArgumentCaptor<BigInteger> misRepCaptor;
    @Captor
    ArgumentCaptor<BigInteger> exMisRepFileCaptor;
    @Captor
    ArgumentCaptor<Pageable> exMisRepFilePageableCaptor;
    @Captor
    ArgumentCaptor<BigInteger> exItemCaptor;
    @Captor
    ArgumentCaptor<Pageable> exItemPageableCaptor;

    /**
     * 観点1.正しい優良誤認検査IDで検索し、各Repositoryに正しい値を渡せていることを確認する.
     * 観点2.正しい優良誤認検査IDで検索し、Serviceが正しい値を返せていることを確認する(OUT).
     * 観点3.優良誤認検査IDは数値
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "000000\n000001", password = "H3RYB9F8", roles = { "USER", "JUN" })
    public void test_get_success() throws Exception {
//        final TMisleadingRepresentationEntity misRepEntity = new TMisleadingRepresentationEntity();
//
//        //渡された優良誤認検査ID(1)を設定
//        misRepEntity.setId(BigInteger.valueOf(1));
//        final List<ExtendedTMisleadingRepresentationFileEntity> expectedMisRepFileList = newMisRepFiles(1, 2);
//        final List<ExtendedTItemEntity> expectedItemList = newItems(1, 2);
//
//        //List<xxxEntity>からList<xxxModel>へ変換
//        List<MisleadingRepresentationFileModel> expectedMisRepFileModelList = castToMisRepFileModel(expectedMisRepFileList);
//        List<ItemModel> expectedItemModelList = castToItemModel(expectedItemList);
//
//        final Pageable page1 = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("file_no_id")));
//        final Pageable page2 = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("part_no")));
//
//        Page<ExtendedTMisleadingRepresentationFileEntity> misRepFilePage = new PageImpl<ExtendedTMisleadingRepresentationFileEntity>(expectedMisRepFileList,
//                page1, expectedMisRepFileList.size());
//        Page<ExtendedTItemEntity> itemPage = new PageImpl<ExtendedTItemEntity>(expectedItemList, page2, expectedItemList.size());
//
//        //モックしたサービスの挙動を設定する：どんな引数の場合でもreを返す
//        doReturn(Optional.of(misRepEntity)).when(tMisRepRepo).findById(any(BigInteger.class));
//        doReturn(misRepFilePage).when(exTMisRepFileRepo).findByMisRepId(any(BigInteger.class), any(Pageable.class));
//        doReturn(itemPage).when(exTItemRepo).findByMisRepId(any(BigInteger.class), any(Pageable.class));
//
//        //Serviceを実行する
//        final GetServiceParameter<BigInteger> param = new GetServiceParameter<BigInteger>(null, BigInteger.valueOf(1));
//        final GetServiceResponse<MisleadingRepresentationModel> actual = misRepGetService.call(param);
//
//        //観点1.
//        //モックしたインスタンス内の関数が呼び出されていることを確認(正しいsqlを呼び出しているか)
//        verify(tMisRepRepo, times(1)).findById(misRepCaptor.capture());
//        verify(exTMisRepFileRepo, times(1)).findByMisRepId(exMisRepFileCaptor.capture(), exMisRepFilePageableCaptor.capture());
//        verify(exTItemRepo, times(1)).findByMisRepId(exItemCaptor.capture(), exItemPageableCaptor.capture());
//
//        //関数に渡しているパラメーターが自分で指定したもの(1)であることを確認
//        assertThat(misRepCaptor.getValue()).isEqualTo(1);
//        assertThat(exMisRepFileCaptor.getValue()).isEqualTo(1);
//        assertThat(exMisRepFilePageableCaptor.getValue().toString()).isEqualTo(page1.toString());
//        assertThat(exItemCaptor.getValue()).isEqualTo(1);
//        assertThat(exItemPageableCaptor.getValue().toString()).isEqualTo(page2.toString());
//
//        //観点2.
//        //Serviceが正しい結果を返していることを確認
//        assertThat(actual.getItem().getId()).isEqualTo(misRepEntity.getId());
//        assertThat(actual.getItem().getMisleadingRepresentationFiles().toString()).isEqualTo(expectedMisRepFileModelList.toString());
//        assertThat(actual.getItem().getItems().toString()).isEqualTo(expectedItemModelList.toString());

    }

//    private List<ExtendedTItemEntity> newItems(final int... ids) {
//        final List<ExtendedTItemEntity> items = new ArrayList<>(ids.length);
//
//        for (final int id :ids) {
//            final ExtendedTItemEntity item = new ExtendedTItemEntity();
//            item.setId(BigInteger.valueOf(new Long(id)));
//            items.add(item);
//        }
//
//        return items;
//    }

//    private List<ExtendedTMisleadingRepresentationFileEntity> newMisRepFiles(final int... ids) {
//        final List<ExtendedTMisleadingRepresentationFileEntity> items = new ArrayList<>(ids.length);
//
//        for (final int id :ids) {
//            final ExtendedTMisleadingRepresentationFileEntity item = new ExtendedTMisleadingRepresentationFileEntity();
//            item.setId(BigInteger.valueOf(new Long(id)));
//            items.add(item);
//        }
//
//        return items;
//    }

//    private List<ItemModel> castToItemModel(final List<ExtendedTItemEntity> entityList) {
//        ItemModel itemModel;
//        final List<ItemModel> items = new ArrayList<>();
//
//        for (ExtendedTItemEntity extendedTItemEntity : entityList) {
//            itemModel = new ItemModel();
//            BeanUtils.copyProperties(extendedTItemEntity, itemModel);
//            items.add(itemModel);
//        }
//
//        return items;
//    }

//    private List<MisleadingRepresentationFileModel> castToMisRepFileModel(final List<ExtendedTMisleadingRepresentationFileEntity> entityList) {
//        MisleadingRepresentationFileModel misRepFileModel;
//        final List<MisleadingRepresentationFileModel> misRepFiles = new ArrayList<>();
//
//        for (ExtendedTMisleadingRepresentationFileEntity extendedTMisRepFileEntity : entityList) {
//            misRepFileModel = new MisleadingRepresentationFileModel();
//            BeanUtils.copyProperties(extendedTMisRepFileEntity, misRepFileModel);
//            misRepFiles.add(misRepFileModel);
//        }
//
//        return misRepFiles;
//    }


}
//CHECKSTYLE:ON

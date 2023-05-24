// CHECKSTYLE:OFF

//TODO

package jp.co.jun.edi.service;

import java.math.BigInteger;

import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.co.jun.edi.repository.TFileRepository;
import jp.co.jun.edi.repository.TItemRepository;
import jp.co.jun.edi.repository.TMisleadingRepresentationFileRepository;

/**
 * MisleadingRepresentationCreateServiceテスト.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
public class MisleadingRepresentationFileCreateServiceTest {

//    @Mock
//    private TMisleadingRepresentationRepository tMisRepRepo;

    @Mock
    private TMisleadingRepresentationFileRepository tMisRepFileRepo;

    @Mock
    private TItemRepository tItemRepo;

    @Mock
    private TFileRepository fileRepo;

    //一部だけをMock化
    @Spy
    private ObjectMapper objectMapper;

    // モックオブジェクトの挿入対象
    @InjectMocks
    private MisleadingRepresentationFileCreateService misRepetCreateService;

    // モックへの引数を取得するためのキャプター
    @Captor
    ArgumentCaptor<BigInteger> fileNoIdCaptor;
    @Captor
    ArgumentCaptor<BigInteger> fileIdCaptor;
    @Captor
    ArgumentCaptor<BigInteger> userIdCaptor;

    /**
     * 観点1.各Repositoryに正しい値を渡せていることを確認する.
     * 観点2.Serviceが正しい値を返せていることを確認する(OUT).
     * 観点3.FileInfoMode.INSERT
     * @throws Exception
     */
    @Test
    public void test_create_success() throws Exception {
//        final MisleadingRepresentationModel misRepModel = new MisleadingRepresentationModel();
//
//        final List<TFileEntity> expectedFileList = newFiles(3);
//        final List<TItemEntity> expectedItemList = newItems(1);
//
//        //List<xxxEntity>からList<xxxModel>へ変換
//        List<FileModel> expectedFileModelList = castToFileModel(expectedFileList,FileInfoMode.INSERT);
//        List<ItemModel> expectedItemModelList = castToItemModel(expectedItemList);
//
//        misRepModel.setFiles(expectedFileModelList);
//        misRepModel.setItems(expectedItemModelList);
//
//        final TItemEntity itemEntity = new TItemEntity();
//        itemEntity.setId(BigInteger.valueOf(1));
//
//        //モックしたRepositoryの挙動を設定する
//        doReturn(Optional.of(itemEntity)).when(tItemRepo).findById(any(BigInteger.class));
//
//        //Serviceを実行する
//        final CreateServiceParameter<MisleadingRepresentationModel> param = new CreateServiceParameter<MisleadingRepresentationModel>(null, misRepModel);
////        final CreateServiceResponse<MisleadingRepresentationModel> actual = misRepetCreateService.call(param);
//
//        //観点1.
//        //モックしたインスタンス内の関数が呼び出されていることを確認(正しいsqlを呼び出しているか)
//        verify(tItemRepo, times(1)).findById(fileNoIdCaptor.capture());
//        //updateByDeleteAtは呼び出されていない
//        verify(fileRepo, times(0)).updateDeleteAtById(fileIdCaptor.capture(), userIdCaptor.capture());
//
//        //関数に渡しているパラメーターが自分で指定したもの(1)であることを確認
//        assertThat(fileNoIdCaptor.getValue()).isEqualTo(1);
//
//        //観点2.
//        //Serviceが正しい結果を返していることを確認
//        misRepModel.setId(BigInteger.valueOf(17));
////        assertThat(actual.getItem().getId()).isEqualTo(misRepModel.getId());
////        //modeの確認もできる
////        assertThat(actual.getItem().getFiles().toString()).isEqualTo(expectedFileModelList.toString());
////        assertThat(actual.getItem().getItems().toString()).isEqualTo(expectedItemModelList.toString());

    }

    /**
     * 観点1.各Repositoryに正しい値を渡せていることを確認する.
     * 観点2.Serviceが正しい値を返せていることを確認する(OUT).
     * 観点3.FileInfoMode.DELETE
     * @throws Exception
     */
    @Test
    public void test_create_success_haveDel() throws Exception {
//        final MisleadingRepresentationModel misRepModel = new MisleadingRepresentationModel();
//
//        final List<TFileEntity> expectedFileList = newFiles(4);
//        final List<TItemEntity> expectedItemList = newItems(1);
//
//        //List<xxxEntity>からList<xxxModel>へ変換
//        List<FileModel> expectedFileModelList = castToFileModel(expectedFileList, FileInfoMode.DELETED);
//        List<ItemModel> expectedItemModelList = castToItemModel(expectedItemList);
//
//        misRepModel.setFiles(expectedFileModelList);
//        misRepModel.setItems(expectedItemModelList);
//
//        final TItemEntity itemEntity = new TItemEntity();
//        itemEntity.setId(BigInteger.valueOf(1));
//
//        //モックしたRepositoryの挙動を設定する
//        doReturn(Optional.of(itemEntity)).when(tItemRepo).findById(any(BigInteger.class));
//        doReturn(1).when(fileRepo).updateDeleteAtById(any(BigInteger.class), any(BigInteger.class));
//
//        //Serviceを実行する
//        final MUserEntity user = new MUserEntity();
//        user.setId(BigInteger.valueOf(1));
//        user.setAccountName("a");
//        user.setAuthority("a");
//        user.setCompany("a");
//        user.setPassword("a");
//        final CustomLoginUser customLoginUser = new CustomLoginUser(user);
//        final CreateServiceParameter<MisleadingRepresentationModel> param = new CreateServiceParameter<MisleadingRepresentationModel>(customLoginUser, misRepModel);
////        final CreateServiceResponse<MisleadingRepresentationModel> actual = misRepetCreateService.call(param);
//
//        //観点1.
//        //モックしたインスタンス内の関数が呼び出されていることを確認(正しいsqlを呼び出しているか)
//        verify(tItemRepo, times(1)).findById(fileNoIdCaptor.capture());
//        //updateByDeleteAtは呼び出されている
//        verify(fileRepo, times(1)).updateDeleteAtById(fileIdCaptor.capture(), userIdCaptor.capture());
//
//        //関数に渡しているパラメーターが自分で指定したもの(1)であることを確認
//        assertThat(fileNoIdCaptor.getValue()).isEqualTo(1);
//        assertThat(fileIdCaptor.getValue()).isEqualTo(4);
//        assertThat(userIdCaptor.getValue()).isEqualTo(1);
//
//        //観点2.
//        //Serviceが正しい結果を返していることを確認
//        misRepModel.setId(BigInteger.valueOf(17));
////        assertThat(actual.getItem().getId()).isEqualTo(misRepModel.getId());
////        assertThat(actual.getItem().getFiles().toString()).isEqualTo(expectedFileModelList.toString());
////        assertThat(actual.getItem().getItems().toString()).isEqualTo(expectedItemModelList.toString());

    }

//    private List<TItemEntity> newItems(final int... ids) {
//        final List<TItemEntity> items = new ArrayList<>(ids.length);
//
//        for (final int id :ids) {
//            final TItemEntity item = new TItemEntity();
//            item.setId(BigInteger.valueOf(new Long(id)));
//            items.add(item);
//        }
//
//        return items;
//    }
//
//    private List<TFileEntity> newFiles(final int... ids) {
//        final List<TFileEntity> items = new ArrayList<>(ids.length);
//
//        for (final int id :ids) {
//            final TFileEntity item = new TFileEntity();
//            item.setId(BigInteger.valueOf(new Long(id)));
//            items.add(item);
//        }
//
//        return items;
//    }

//    private List<ItemModel> castToItemModel(final List<TItemEntity> entityList) {
//        ItemModel itemModel;
//        final List<ItemModel> items = new ArrayList<>();
//
//        for (TItemEntity tItemEntity : entityList) {
//            itemModel = new ItemModel();
//            BeanUtils.copyProperties(tItemEntity, itemModel);
//            items.add(itemModel);
//        }
//
//        return items;
//    }

//    private List<FileModel> castToFileModel(final List<TFileEntity> entityList , FileInfoMode mode) {
//        FileModel fileModel;
//        final List<FileModel> files = new ArrayList<>();
//
//        for (TFileEntity TFileEntity : entityList) {
//            fileModel = new FileModel();
//            BeanUtils.copyProperties(TFileEntity, fileModel);
////            fileModel.setMode(mode);
//            files.add(fileModel);
//        }
//
//        return files;
//    }


}
//CHECKSTYLE:ON

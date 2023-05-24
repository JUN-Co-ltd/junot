package jp.co.jun.edi.repository;

import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * TMisleadingRepresentationRepositoryテスト.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
public class TMisleadingRepresentationRepositoryTest {

//    @Autowired
//    private TMisleadingRepresentationRepository tMisRepRepo;

    /**
     * DBに存在する,削除日時が空の
     * 優良誤認検査IDで検索し、正常にレスポンスされることを確認する.
     */
    @Test
    public void test_findById_inDB() {
//        try {
//            //初期値：DBデータファイルパス
//            final String initFilePath = new RepositoryTest().getFileAbsolutePath("/MisRepRepo");
//            final CSVDataSetController cSVDataSetController = new CSVDataSetController();
//            // テストデータ投入
//            cSVDataSetController.setUp(initFilePath);
//
//            //期待値
//            final TMisleadingRepresentationEntity misRepEntity = new TMisleadingRepresentationEntity();
//            misRepEntity.setId(BigInteger.valueOf(1));
//
//            //実値(sql実行)
//            Optional<TMisleadingRepresentationEntity> actual = tMisRepRepo.findById(BigInteger.valueOf(1));
//
//            //テスト実行
//            assertThat(actual.get().getId()).isEqualTo(misRepEntity.getId());
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            fail(e.getMessage());
//        }
    }

    /**
     * DBに存在する,削除日時が空でない
     * 優良誤認検査IDで検索し、なにも返さないことを確認する.
     */
    @Test
    public void test_findById_inDB_del() {
//        try {
//            //初期値：DBデータファイルパス
//            final String initFilePath = new RepositoryTest().getFileAbsolutePath("/MisRepRepo");
//            final CSVDataSetController cSVDataSetController = new CSVDataSetController();
//            // テストデータ投入
//            cSVDataSetController.setUp(initFilePath);
//
//            //実値(sql実行)
//            Optional<TMisleadingRepresentationEntity> actual = tMisRepRepo.findById(BigInteger.valueOf(2));
//
//            assertThat(actual.isPresent()).isEqualTo(false); //データがないことを確認
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            fail(e.getMessage());
//        }
    }

    /**
     * DBに存在しない優良誤認検査IDで検索し、何も返さないことを確認する.
     */
    @Test
    public void test_findById_notInDB() {
//        try {
//            //初期値：DBデータファイルパス
//            final String initFilePath = new RepositoryTest().getFileAbsolutePath("/MisRepRepo");
//            final CSVDataSetController cSVDataSetController = new CSVDataSetController();
//            // テストデータ投入
//            cSVDataSetController.setUp(initFilePath);
//
//            //実値(sql実行)
//            Optional<TMisleadingRepresentationEntity> actual = tMisRepRepo.findById(BigInteger.valueOf(45));
//
//            assertThat(actual.isPresent()).isEqualTo(false);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            fail(e.getMessage());
//        }
    }

}

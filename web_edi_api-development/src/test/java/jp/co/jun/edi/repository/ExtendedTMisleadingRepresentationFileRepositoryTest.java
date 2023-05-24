package jp.co.jun.edi.repository;

import static org.junit.Assert.*;

import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import test.utils.CSVDataSetController;

/**
 * ExtendedTMisleadingRepresentationFileRepositoryテスト.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
public class ExtendedTMisleadingRepresentationFileRepositoryTest {

//    @Autowired
//    private ExtendedTMisleadingRepresentationFileRepository exTMisRepFileRepo;

    /**
     * DBに存在する,削除日時が空の
     * 優良誤認検査IDで検索し、正常にレスポンスされることを確認する.
     */
    @Test
    public void test_findByMisRepId_inDB() {
        try {

            //初期値：DBデータファイルパス
            final String initFilePath = new RepositoryTest().getFileAbsolutePath("/ExMisRepFileRepo");
            final CSVDataSetController cSVDataSetController = new CSVDataSetController();
            // テストデータ投入
            cSVDataSetController.setUp(initFilePath);

            //期待値：
//            final List<BigInteger> expected = Arrays.asList(BigInteger.valueOf(1), BigInteger.valueOf(2));

//            //実値(sql実行)
//            Page<ExtendedTMisleadingRepresentationFileEntity> actual = exTMisRepFileRepo.findByMisRepId(BigInteger.valueOf(1),
//                    PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("file_no_id"))));
//
//            //テスト実行
//            //データの数の確認
//            assertThat(actual.getContent().size()).isEqualTo(expected.size());
//            //データの値の確認
//            for (int i = 0; i < actual.getContent().size(); i++) {
//                assertThat(actual.getContent().get(i).getId()).isEqualTo(expected.get(i));
//            }

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    /**
     * DBに存在する,削除日時が空でない
     * 優良誤認検査IDで検索し、なにも返さないことを確認する.
     */
    @Test
    public void test_findByMisRepId_inDB_del() {
        try {
            //初期値：DBデータファイルパス
            final String initFilePath = new RepositoryTest().getFileAbsolutePath("/ExMisRepFileRepo");
            final CSVDataSetController cSVDataSetController = new CSVDataSetController();
            // テストデータ投入
            cSVDataSetController.setUp(initFilePath);

            //実値(sql実行)
//            Page<ExtendedTMisleadingRepresentationFileEntity> actual = exTMisRepFileRepo.findByMisRepId(BigInteger.valueOf(4), null);
//
//            //テスト実行
//            assertThat(actual.getContent().size()).isEqualTo(0);

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * DBに存在しない優良誤認検査IDで検索し、何も返さないことを確認する.
     */
    @Test
    public void test_findByMisRepId_notInDB() {
        try {
            //初期値：DBデータファイルパス
            final String initFilePath = new RepositoryTest().getFileAbsolutePath("/ExMisRepFileRepo");
            final CSVDataSetController cSVDataSetController = new CSVDataSetController();
            // テストデータ投入
            cSVDataSetController.setUp(initFilePath);

//            //実値(sql実行)
//            Page<ExtendedTMisleadingRepresentationFileEntity> actual = exTMisRepFileRepo.findByMisRepId(BigInteger.valueOf(45), null);

            //テスト実行
//            assertThat(actual.getContent().size()).isEqualTo(0);

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

}

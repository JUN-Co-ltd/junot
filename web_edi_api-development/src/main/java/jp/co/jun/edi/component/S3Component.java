package jp.co.jun.edi.component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
/**
 * S3コンポーネント.
 */
@Component
public class S3Component extends GenericComponent {

    @Autowired
    private AmazonS3 amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * S3からファイルをダウンロードする.
     * @param key String
     * @return ResponseEntity
     * @throws IOException IOException
     */
    public byte[] download(final String key) throws IOException {
        final GetObjectRequest getObjectRequest = new GetObjectRequest(bucket, key);
        final S3Object s3Object = amazonS3Client.getObject(getObjectRequest);
        final S3ObjectInputStream objectInputStream = s3Object.getObjectContent();
        return IOUtils.toByteArray(objectInputStream);
    }

    /**
     * S3からファイルをダウンロードし、一時保存パスに保存する.
     * @param key String
     * @param file File
     * @throws IOException IOException
     */
    public void downloadFileToTmpDirectory(final String key, final File file) throws IOException {
        final GetObjectRequest getObjectRequest = new GetObjectRequest(bucket, key);
        amazonS3Client.getObject(getObjectRequest, file);
    }

    /**
     * S3にファイルをアップロードする.
     * @param file アップロードファイル
     * @param s3prefix S3プレフィックス
     * @param contentType ContentType
     * @return s3Key
     * @throws IOException IOException
     */
    public String upload(final File file, final String s3prefix, final String contentType) throws IOException {
        final String s3Key = generateS3Key(file.getName());
        final PutObjectRequest request = new PutObjectRequest(bucket + "/" + s3prefix, s3Key, file);
        amazonS3Client.putObject(request);
        return s3Key;
    }

    /**
     * S3キーを作成する.
     * @param fileName ファイル名
     * @return S3キー
     */
    private String generateS3Key(final String fileName) {
        final UUID uuid = UUID.randomUUID();
        final String extension = getExtension(fileName);
        return uuid.toString() + "." + extension;
    }

    /**
     * 拡張子を取得する.
     * @param fileName ファイル名
     * @return 拡張子
     */
    private String getExtension(final String fileName) {
        if (fileName == null) {
            return null;
        }

        // 最後の『 . 』の位置を取得します。
        final int lastDotPosition = fileName.lastIndexOf(".");

        if (lastDotPosition == -1) {
            return null;
        }

        // 『 . 』が存在する場合は、『 . 』以降を返します。
        return fileName.substring(lastDotPosition + 1);
    }

    /**
     * 指定されたフォルダ配下のフォルダ以外のオブジェクト概要のリストを最終更新日時の昇順で取得する.
     * リストは全件取得するため、OutOfMemoryが発生しないように取得対象のフォルダは限定すること。
     *
     * @param s3prefix S3プレフィックス
     * @return オブジェクト概要のリスト（オブジェクトのデータは含まれていない）
     */
    public List<S3ObjectSummary> getObjectSummaries(final String s3prefix) {
        final ListObjectsV2Request request = new ListObjectsV2Request()
                .withBucketName(bucket)
                .withPrefix(s3prefix);

        final List<S3ObjectSummary> objects = new ArrayList<>();

        ListObjectsV2Result result = amazonS3Client.listObjectsV2(request);

        addObjectSummaries(result, objects);

        while (result.isTruncated()) {
            request.setContinuationToken(result.getNextContinuationToken());
            result = amazonS3Client.listObjectsV2(request);

            addObjectSummaries(result, objects);
        }

        // 最終更新日時の昇順でソート
        Collections.sort(objects, Comparator.comparing(S3ObjectSummary::getLastModified));

        return objects;
    }


    /**
     * オブジェクトを移動する.
     *
     * @param sourceKey 移動元オブジェクトのパス
     * @param destinationKey 移動先オブジェクトのパス
     */
    public void moveObject(
            final String sourceKey,
            final String destinationKey) {
        amazonS3Client.copyObject(bucket, sourceKey, bucket, destinationKey);

        amazonS3Client.deleteObject(bucket, sourceKey);
    }

    /**
     * フォルダ以外のオブジェクトをリストに追加する.
     *
     * @param result S3の結果
     * @param objects オブジェクトのリスト
     */
    private void addObjectSummaries(final ListObjectsV2Result result, final List<S3ObjectSummary> objects) {
        for (final S3ObjectSummary os : result.getObjectSummaries()) {
            if (!StringUtils.endsWith(os.getKey(), "/")) {
                // フォルダを除く
                objects.add(os);
            }
        }
    }
}

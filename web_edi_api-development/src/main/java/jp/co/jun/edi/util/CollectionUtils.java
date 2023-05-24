package jp.co.jun.edi.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * CollectionUtils.
 *
 */
public final class CollectionUtils {


    /**
     */
    private CollectionUtils() {
    }

    /**
     * リストを任意の要素数ごとに分割する.
     * <pre>
     * chunk(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), 3);
     * Arrays.asList(
     *               Arrays.asList(1, 2, 3),
     *               Arrays.asList(4, 5, 6),
     *               Arrays.asList(7, 8, 9),
     *               Arrays.asList(10)
     * )
     * </pre>
     * @param <T> リスト
     * @param origin 分割したいリスト
     * @param size 分割したい要素数
     * @return 分割されたリスト
     */
    public static <T> List<List<T>> chunk(final List<T> origin, final int size) {
        if (origin == null || origin.isEmpty() || size <= 0) {
            return Collections.emptyList();
        }

        // 余剰計算
        int add = 0;
        if (origin.size() % size > 0) {
            add = 1;
        }

        int block = origin.size() / size + add;

        return IntStream.range(0, block)
                .boxed()
                .map(i -> {
                    int start = i * size;
                    int end = Math.min(start + size, origin.size());
                    return origin.subList(start, end);
                })
                .collect(Collectors.toList());
    }

    /**
     * 配列を結合しListとして返す.
     * @param list1 配列1
     * @param lists 配列2...
     * @return リスト
     */
    public static List<String> concatArrayToList(final String[] list1, final String[]... lists) {
        final List<String> concatedList = new ArrayList<String>();
        concatedList.addAll(Arrays.asList(list1));
        for (String[] list : lists) {
            concatedList.addAll(Arrays.asList(list));
        }

        return concatedList;
    }

    /**
     * キー重複除去.
     * @param <T> T
     * @param keyExtractor キー
     * @return キー重複除去
     */
    public static <T> Predicate<T> distinctByKey(final Function<? super T, ?> keyExtractor) {
      final Map<Object, Boolean> seen = new ConcurrentHashMap<>();
      return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    /**
     * 空かどうか確認.
     * @param <T> T
     * @param coll 一覧
     * @return true:空/false:空ではない
     */
    public static <T> boolean isEmpty(final List<T> coll) {
        return org.apache.commons.collections.CollectionUtils.isEmpty(coll);
    }
}

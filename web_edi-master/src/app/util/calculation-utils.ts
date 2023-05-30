import * as Moment_ from 'moment';
import { StringUtils } from './string-utils';
import { NgbDate } from '@ng-bootstrap/ng-bootstrap';

const Moment = Moment_;

/**
 * 計算ユーティリティ.
 */
export class CalculationUtils {
  constructor(
  ) { }

  /**
   * 率計算(String→number変換).
   * 分子、分母の入力チェックを行い、率計算の結果を返します。
   *
   * @param numerator 分子
   * @param denominator 分母
   * @returns number型の率
   */
  static calcRate(numeratorStr: string, denominatorStr: string): number {

    if (StringUtils.isNotEmpty(numeratorStr)) {
      numeratorStr = numeratorStr.toString().replace(/,/g, '');
    }
    if (StringUtils.isNotEmpty(denominatorStr)) {
      denominatorStr = denominatorStr.toString().replace(/,/g, '');
    }

    const numerator = Number(numeratorStr);
    const denominator = Number(denominatorStr);

    return this.calcRateIsNumber(numerator, denominator);
  }

  /**
   * 率計算.
   * 分子、分母の入力チェックを行い、率計算の結果を返します。
   *
   * @param numerator 分子
   * @param denominator 分母
   * @returns number型の率
   */
  static calcRateIsNumber(numerator: number, denominator: number): number {

    if (!Number.isFinite(numerator) || !Number.isFinite(denominator) || numerator === 0 || denominator === 0) {
      return 0;
    }

    const rate = (numerator / denominator) * 100;
    if (rate < 0) {
      // 算出結果が0より小さい場合
      return Math.ceil(rate * Math.pow(10, 1)) / Math.pow(10, 1);
    } else {
      // 算出結果が0より大きい場合
      return Math.floor(rate * Math.pow(10, 1)) / Math.pow(10, 1);
    }
  }

  /**
   * 週計算.
   * 日付入力項目の値を基に、年間の通し週番号を返します。
   * 月曜日基準で1/1が日曜日の場合は、その週は第1週として扱います。
   * その他の場合は、12/31が含まれる週は翌年の第1週として扱います。
   * 計算不可の場合、nullを返却します。
   *
   * @param inDate 日付入力値
   * @returns number 年間の通し週番号
   */
  static calcWeek(inDate: NgbDate): number {
    // 入力値の日付を取得
    if (!inDate) {
      return null;
    }

    // console.debug('月曜基準の週番号===', Moment([inDate.year, inDate.month - 1, inDate.day]).isoWeekday(1).week());

    // ロケール設定
    Moment.locale('ja');

    // 月曜基準の返却する週番号
    const weekNum = Moment([inDate.year, inDate.month - 1, inDate.day]).isoWeekday(1).week();
    // 月曜基準の一年最後の週番号
    const endWeekNum = Moment([inDate.year, inDate.month - 1, inDate.day]).endOf('year').isoWeekday(1).week();

    // 次年の1/1の曜日を取得
    const firstDayOfTheNextYear = Moment([inDate.year + 1, inDate.month - 1, inDate.day]).startOf('year').weekday();
    // 当年の1/1の曜日を取得
    const firstDayOfTheYear = Moment([inDate.year, inDate.month - 1, inDate.day]).startOf('year').weekday();
    if (isNaN(firstDayOfTheYear)) {
      return null;
    }

    // 入力値が1/1か判定
    const isFirstDay = Moment([inDate.year, inDate.month - 1, inDate.day]).isSame(
      Moment([inDate.year, inDate.month - 1, inDate.day]).startOf('year'), 'day');

    // 1/1が日曜なら
    if (firstDayOfTheYear === 0) {
      // 1/1を入力した場合週番号は1を返す
      if (isFirstDay) {
        return 1;
      }
      // その他は 月曜基準+１ を返却
      return weekNum + 1;
    }

    // 次の年の1/1が日曜 かつ 当年最後の週なら
    // 翌年の第1週となるので、1を返す
    if (firstDayOfTheNextYear === 0 && weekNum === endWeekNum) {
      return 1;
    }

    // その他は、月曜基準で返却
    return weekNum;
  }

  /**
   * 逆・週計算.
   * 入力された週番号を基に、日付を返します。日付は該当週の(週末)日曜日。
   * 月曜日基準で1/1が含まれる週を第1週として扱います。
   * 12/31が日曜日の場合は、次の週が第1週となり、それ以外はその週が第1週となります。
   * 計算不可の場合、nullを返却します。
   *
   * @param inWeekNum 週番号入力値
   * @param inYear 品番の年度部分入力値
   * @returns Moment 年・週番号から算出した日付※日曜日
   */
  static calcWeekDate(inWeekNumStr: string | number, inYear: string | number): string {
    const inWeekNum = Number(inWeekNumStr);
    // 入力値の週番号を取得
    if (!inWeekNum || inWeekNum === 0 || inWeekNum < 0) { // 週番号が空/0/負数になれば日付も空にする
      return null;
    }

    if (!inYear) { // 年度が入力されてない場合は、当年度とする
      const now = Moment();
      now.locale('ja');
      inYear = now.year().toString();
    }

    // 12/31の曜日を取得
    const endDayOfTheYear = Moment(inYear, 'YYYY/MM/DD', 'ja').endOf('year').weekday();
    if (isNaN(endDayOfTheYear)) {
      return null;
    }

    // 12/31が日曜なら
    if (endDayOfTheYear === 0) {
      // 次の週が第一週
      // 日曜日を返す
      return Moment(inYear, 'YYYY/MM/DD', 'ja').add(inWeekNum, 'weeks').endOf('isoWeek').format('YYYY/MM/DD');
    }

    // その他は、その週が第一週
    // 日曜日を返す
    return Moment(inYear, 'YYYY/MM/DD', 'ja').add(inWeekNum - 1, 'weeks').endOf('isoWeek').format('YYYY/MM/DD');
  }

  /**
   * 日計日をから納品可能範囲のスタートを算出
   * 計算不可の場合、nullを返却します。
   *
   * @param nitYmd 日計日
   * @returns Moment 日計日
   */
  static calcStartDeliveryAt(nitYmd: string): string {
    // DBから日計日を取得
    if (!nitYmd || nitYmd === '') { // DBから取得できなかった場合
      return null;
    }

    // 日計日を返す
    return Moment(nitYmd, 'YYYYMMDD', 'ja').format('YYYY/MM/DD');
  }

  /**
   * 月次締日から納品可能範囲のエンドを算出
   * 計算不可の場合、nullを返却します。
   *
   * @param simYmd 月次締日
   * @returns Moment 月次締日+1年
   */
  static calcEndDeliveryAt(simYmd: string): string {
    if (!simYmd || simYmd === '') {
      return null;
    }

    // 月次締日+1年を返す
    return Moment(simYmd, 'YYYYMMDD', 'ja').add(1, 'years').format('YYYY/MM/DD');
  }

  /**
   * 月次締日から発注可能範囲のスタートを算出
   * @param simYmd 月度締め日
   * @returns Moment 前月月度締め日+1日の日付
   */
  static calcStartOrderAt(simYmd: string): string {
    if (!simYmd || simYmd === '') {
      return null;
    }

    // 月次締日の前月+1日を返す
    return Moment(simYmd, 'YYYYMMDD', 'ja').add({ days: 1, months: -1 }).format('YYYY/MM/DD');
  }

  /**
   * 月次締日から発注可能範囲のエンドを算出
   * @param simYmd 月度締め日
   * @returns Moment 前月月度締め日の翌月
   */
  static calcEndOrderAt(simYmd: string): string {
    if (!simYmd || simYmd === '') {
      return null;
    }

    // 月次締日の前月+1日を返す
    return Moment(simYmd, 'YYYYMMDD', 'ja').add({ months: 1 }).format('YYYY/MM/DD');
  }

  /**
   * 通番を基に会計年度を算出する。
   * 通番2桁目　=　年の下1桁  →　現在年を返却
   * 通番2桁目　>　年の下1桁  →　現在年の上3桁と通番2桁目を結合して返却
   * 通番2桁目　<　年の下1桁
   * 　 - 通番2桁目　=　前年の下1桁   →　前年を返却
   * 　 - 通番2桁目　!=　前年の下1桁  →　現在年の上3桁目+1と通番2桁目を結合して返却(2年前以上は10年加算)
   * @param partNoSerialNo 通番
   */
  static calFiscalYear(partNoSerialNo: string): number {
    // 数値以外、または1桁は処理しない
    const regex = new RegExp(/^[0-9]+$/);
    if (!regex.test(partNoSerialNo) || partNoSerialNo.length < 2) { return null; }

    const now = Moment();
    now.locale('ja');
    const currentYear = String(now.format('YYYY')); // 現在年
    const previousYear = String(now.subtract(1, 'years').format('YYYY')); // 前年
    const secondDigit = partNoSerialNo.substr(1, 1);  // 通番2桁目

    let fiscalYear;
    const currentEndDigit = currentYear.substr(3, 1);
    if (secondDigit === currentEndDigit) {  // 通番2桁目と現在の年の末尾が同じ
      fiscalYear = currentYear;  // 現在年をセットする。
    } else if (secondDigit > currentEndDigit) { // 通番2桁目が現在の年より大きい
      fiscalYear = currentYear.substr(0, 3) + secondDigit;  // 現在年の上3桁と通番2桁目を結合
    } else {
      if (secondDigit === previousYear.substr(3, 1)) {  // 通番2桁目と前年の末尾が同じ
        fiscalYear = previousYear;  // 前年をセットする。
      } else {
        fiscalYear = String(Number(currentYear.substr(0, 3)) + 1) + secondDigit; // 現在年の上3桁+1と通番2桁目を結合
      }
    }
    return Number(fiscalYear);
  }

  /*
   * 納品日を基に投入日を算出する。
   * 投入日＝納品日
   * @param preferredDeliveryDate 納品日
   * @returns Moment 納品日
   */
  static calcDeploymentDate(preferredDeliveryDate: string): string {
    if (!preferredDeliveryDate || preferredDeliveryDate === '') {
      return null;
    }

    // 不適切入力の場合、計算しない
    if (!Moment(preferredDeliveryDate).isValid()) {
      return null;
    }
    // 投入日＝納品日を返す
    return Moment(preferredDeliveryDate, 'YYYY/MM/DD', 'ja').format('YYYY/MM/DD');
  }

  /**
   * 投入日を基にP終了日を算出する。
   * P終了日=投入日+加算日
   * @param deploymentDate 投入日
   * @returns Moment 納品日
   */
  static calcPendDate(deploymentDate: string, addDay: number): string {
    if (!deploymentDate || deploymentDate === '') {
      return null;
    }

    // 不適切入力の場合、計算しない
    if (!Moment(deploymentDate).isValid()) {
      return null;
    }
    // P終了日=投入日+加算日を返す
    return Moment(deploymentDate, 'YYYY/MM/DD', 'ja').add(addDay, 'days').format('YYYY/MM/DD');
  }

}

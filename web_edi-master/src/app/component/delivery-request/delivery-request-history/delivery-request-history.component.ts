import { Component, OnInit, Input } from '@angular/core';

import { DeliveryApprovalStatus, Path } from '../../../const/const';
import { FileUtils } from '../../../util/file-utils';
import { DateUtils } from '../../../util/date-utils';

import { FileService } from '../../../service/file.service';

import { OrderSku } from '../../../model/order-sku';
import { Delivery } from '../../../model/delivery';
import { DeliverySku } from '../../../model/delivery-sku';
import { DeliveryHistoryForView } from '../../../model/delivery-history-for-view';
import { DeliveryFileInfo } from '../../../model/delivery-file-info';

@Component({
  selector: 'app-delivery-request-history',
  templateUrl: './delivery-request-history.component.html',
  styleUrls: ['./delivery-request-history.component.scss']
})
export class DeliveryRequestHistoryComponent implements OnInit {
  @Input() orderSkuList: OrderSku[] = [];                 // 発注sku情報リスト
  @Input() private deliveryHistoryList: Delivery[] = [];  // 納品履歴リスト
  @Input() nextUrl = '';  // 他の過去納品のURL

  // htmlから参照したい定数を定義
  readonly APPROVAL_STATUS = DeliveryApprovalStatus;
  readonly PATH = Path;

  groupedDeliveryHistoryList: DeliveryHistoryForView[] = [];  // 納品ID・納期・納品依頼回数ごとの納品依頼履歴リスト
  private deliveryFileList: { id: number, fileName: string, fileBlob: Blob }[] = [];  // 納品依頼ファイルの実態リスト(キャッシュ保存用)
  fileDLErrorMessageCode: string = null;  // ファイルダウンロードエラーメッセージコード

  constructor(
    private fileService: FileService
  ) { }

  ngOnInit() {
    // 画面表示用の納品依頼履歴リスト作成
    this.groupedDeliveryHistoryList = this.generateDeliveryHistoryListGroupByKey(
      this.deliveryHistoryList, this.orderSkuList);
  }

  /**
   * キー(納品ID・納期・納品依頼回数)ごとにグルーピングされたリストを生成して返す
   * @param deliveryHistoryList 納品履歴リスト
   * @param orderSkuList 発注SKU情報リスト
   * @return 画面表示用納品依頼履歴リスト
   */
  private generateDeliveryHistoryListGroupByKey(deliveryHistoryList: Delivery[],
    orderSkuList: OrderSku[]): DeliveryHistoryForView[] {
    const groupedDeliveryHistoryList: DeliveryHistoryForView[] = [];

    // (過去)納品依頼ごとの処理
    deliveryHistoryList.forEach(deliveryHistory => {
      // console.debug('■□■□■□■納品依頼ごとのループ■□■□■□■ id:', deliveryHistory.id);
      const deliveryApproveStatus = deliveryHistory.deliveryApproveStatus;  // 承認ステータス保持
      const orderId = deliveryHistory.orderId;
      // 1列作成(キー単位)
      const groupedColumnsByKey = Array.from(
        // 納品依頼明細ごとの処理。mapの中はループする度に前処理までの情報を保持している
        deliveryHistory.deliveryDetails.reduce((map, { id, deliveryId, deliveryFileInfo,
          deliveryRequestAt, correctionAt, deliveryCount, deliverySkus }) => {
          const keyString = JSON.stringify({ deliveryId, correctionAt, deliveryCount });  // 3項目をキーとして設定する為string化
          // console.debug('======納品依頼明細ごとのループ====== id(detail):', id);
          const deliveryHistData = this.generateDeliveryHistoryData(deliveryId, orderId, deliveryFileInfo, deliveryRequestAt, correctionAt,
            deliveryCount, deliveryApproveStatus, orderSkuList, deliverySkus, map, keyString);  // 画面で必要なデータ作成
          return map.set(keyString, deliveryHistData);  // キーと画面表示用のデータを返す
        }, new Map),  // キーごとにまとめる為、整形したリストをmap型で取得
        ([key, deliveryHistData]) => ({ key, deliveryHistData }));  // Array.fromで返す値
      // 1列ごとに画面表示用のデータを納品依頼履歴リストにpush
      groupedColumnsByKey.forEach(columns => groupedDeliveryHistoryList.push(columns.deliveryHistData));
    });

    return this.sortList(groupedDeliveryHistoryList);
  }

  /**
   * 画面用の納品依頼履歴データを作成して返す
   * @param deliveryId 納品ID
   * @param orderId 発注ID
   * @param deliveryFileInfo 納品依頼ファイル情報
   * @param deliveryRequestAt 納品依頼日
   * @param correctionAt 修正納期
   * @param deliveryCount 納品依頼回数
   * @param deliveryApproveStatus 承認ステータス
   * @param orderSkuList 発注SKU情報リスト
   * @param deliverySkus 納期SKUリスト
   * @param map 納品依頼履歴mapデータ
   * @param keyString 納品依頼履歴mapデータのkey
   * @return 画面用の納品依頼履歴データ
   */
  private generateDeliveryHistoryData(deliveryId: number, orderId: number, deliveryFileInfo: DeliveryFileInfo, deliveryRequestAt: Date,
    correctionAt: Date, deliveryCount: number,
    deliveryApproveStatus: string, orderSkuList: OrderSku[], deliverySkus: DeliverySku[], map: Map<any, any>,
    keyString: string): DeliveryHistoryForView {

    // ファイルダウンロードアイコンを表示可能かチェック
    const isAbleToDLFile = deliveryFileInfo != null
      && DateUtils.isWithinPeriod(new Date(), deliveryFileInfo.publishedAt, deliveryFileInfo.publishedEndAt);
    return {
      id: deliveryId,
      orderId: orderId,
      deliveryFileNoId: isAbleToDLFile ? deliveryFileInfo.fileNoId : null,
      deliveryRequestAt: deliveryRequestAt,
      deliveryAt: correctionAt,
      deliveryCount: deliveryCount,
      deliveryApproveStatus: deliveryApproveStatus,
      deliveryLotSum: this.sumDeliveryLotByKey(deliverySkus, map, keyString),
      deliveryLotSumListBySku: this.generateDeliveryLotSumListBySkuInKey(orderSkuList, deliverySkus, map, keyString)
    } as DeliveryHistoryForView;
  }

  /**
   * キーごとの納品依頼数を合計して返す
   * @param deliverySkus 処理中の納品明細情報の納品SKUリスト
   * @param map 作成中の納品依頼履歴マップ
   * @param keyString 作成中の納品依頼履歴マップのキー
   * @return キーごとの納品依頼数合計
   */
  private sumDeliveryLotByKey(deliverySkus: DeliverySku[], map: Map<any, any>, keyString: string): number {
    // 1列の中で加算処理。sumは前処理までの合計を保持する。,0：数値で返す
    const currentDeliveryLotSum = deliverySkus.reduce((sum, { deliveryLot }) => sum + deliveryLot, 0);
    // これまでのループで処理してきたその他の列から同一キーの合計値に加算
    const mapData = map.get(keyString);
    // console.debug('--sumDeliveryLotByKey関数-- mapData:', mapData);
    const prevDeliveryLotSum = mapData != null ? mapData.deliveryLotSum : 0;
    return prevDeliveryLotSum + currentDeliveryLotSum;
  }

  /**
   * (キーごと内の)SKUごとの納品依頼数合計リストを作成して返す.
   * →1列の中のSKU別のリスト(行)を作成する
   * @param orderSkuList 発注SKU情報リスト
   * @param deliverySkus (キー別の)処理中の納品SKUリスト
   * @return SKUごとの納品依頼数合計リスト
   */
  private generateDeliveryLotSumListBySkuInKey(orderSkuList: OrderSku[], deliverySkus: DeliverySku[],
    map: Map<any, any>, keyString: string): number[] {
    const mapData = map.get(keyString);
    const deliveryLotSumListBySku = mapData != null ? mapData.deliveryLotSumListBySku : null; // 今まで処理したループの合計値を取得(キーごとに加算されている)
    // console.debug('--generateDeliveryLotSumListBySkuInKey関数-- deliveryLotSumListBySku:', deliveryLotSumListBySku);
    const deliveryLotList: number[] = [];
    // 発注SKUごとに処理
    orderSkuList.forEach((orderSku, idx) => {
      // 処理中のSKU行について、これまでのループ処理で合計した値があれば取得。なければ0
      let currentDeliveryLotSumListBySku =
        (deliveryLotSumListBySku != null && deliveryLotSumListBySku[idx] != null) ? deliveryLotSumListBySku[idx] : 0;
      // (キー内の)納品SKUごとに処理
      deliverySkus.some(deliverySku => {
        // 処理中のSKUに該当すれば納品依頼数をこれまでのループで処理してきた合計値に加算
        if (orderSku.colorCode === deliverySku.colorCode && orderSku.size === deliverySku.size) {
          currentDeliveryLotSumListBySku += deliverySku.deliveryLot;
          return true;
        }
      });
      // 加算結果を処理中のSKU行に格納
      deliveryLotList[idx] = currentDeliveryLotSumListBySku;
    });
    return deliveryLotList;
  }

  /**
   * 納品依頼履歴リストをソートして返す
   * @param groupedDeliveryHistoryList 納品依頼履歴リスト
   * @return ソート済納品依頼履歴リスト
   */
  private sortList(groupedDeliveryHistoryList: DeliveryHistoryForView[]): DeliveryHistoryForView[] {
    const copyOfGroupedDeliveryHistoryList = [].concat(groupedDeliveryHistoryList);
    return copyOfGroupedDeliveryHistoryList.sort((a, b) => {
      // 1:納期昇順
      if (a.deliveryAt < b.deliveryAt) { return -1; }
      if (a.deliveryAt > b.deliveryAt) { return 1; }
      // 2:納品依頼回数昇順
      if (a.deliveryCount < b.deliveryCount) { return -1; }
      if (a.deliveryCount > b.deliveryCount) { return 1; }
      // 3:納品ID昇順
      return a.id - b.id;
    });
  }

  /**
   * 納品依頼書アイコン押下処理
   * @param deliveryFileNoId ファイルID
   */
  onFileDownload(deliveryFileNoId: number): void {
    this.fileDLErrorMessageCode = null;
    const idExists = this.deliveryFileList.some(deliveryFile => {
      if (deliveryFile.id === deliveryFileNoId) { // 一度ファイルをダウンロードしている
        FileUtils.downloadFile(deliveryFile.fileBlob, deliveryFile.fileName);
        return true;
      }
    });

    // キャッシュにない場合はAPIから取得
    if (!idExists) {
      this.fileService.fileDownload(deliveryFileNoId.toString()).subscribe(res => {
        const data = this.fileService.splitBlobAndFileName(res);
        this.deliveryFileList.push({ id: deliveryFileNoId, fileName: data.fileName, fileBlob: data.blob });
        FileUtils.downloadFile(data.blob, data.fileName);
      }, () => this.fileDLErrorMessageCode = 'ERRORS.FILE_DL_ERROR');
    }
  }
}

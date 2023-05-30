//PRD_0133 #10181 add JFE start
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { ScrollEvent } from 'ngx-scroll-event';
import { PurchaseRecordSearchResult } from 'src/app/model/purchase-record-search-result';
import { PurchaseRecordListStoreService } from '../store/purchase-record-list-store.service';
import { tap, map } from 'rxjs/operators';
import { CarryType } from 'src/app/const/const';
import { FileUtils } from '../../../util/file-utils';
import { FileService } from 'src/app/service/file.service';

@Component({
  selector: 'app-purchase-record-list-result',
  templateUrl: './purchase-record-list-result.component.html',
  styleUrls: ['./purchase-record-list-result.component.scss']
})
export class PurchaseRecordListResultComponent implements OnInit {

  /** 直送. */
  readonly DIRECT_CARRY = CarryType.DIRECT;

  /**  ファイルダウンロードエラーメッセージコード. */
  @Input()
  fileDLErrorMessageCode: string = null;

  /** // 受領書の実態リスト(キャッシュ保存用). */
  private voucherFileList: { id: number, fileName: string, fileBlob: Blob }[] = [];

  /** 最下部へのスクロールイベント */
  @Output()
  private scrollToBottom = new EventEmitter();

  /** チェックon offイベント */
  @Output()
  private checkEvents = new EventEmitter();

  /** フォーム */
  resultForm$ = this.store.resultFormSubject.asObservable();

  constructor(
    private store: PurchaseRecordListStoreService,
    private fileService: FileService
  ) { }

  ngOnInit() {
  }

  /**
   * チェックボックス押下時の処理.
   */
  onCheck(): void {
    this.store.resultFormValue$.pipe(
      map(val => val.purchases.some(p => p.check)),
      tap(somCheck => this.checkEvents.emit(somCheck))
    ).subscribe();
  }

  /**
 * 伝票No押下処理
 * @param purchaseVoucherNumber 伝票No
 */
  onClickVoucherNumBerLink(purchaseVoucherNumber: number): void {
    this.fileDLErrorMessageCode = null;
    const idExists = this.voucherFileList.some(voucherFile => {
      if (voucherFile.id === purchaseVoucherNumber) { // 一度ファイルをダウンロードしている
        FileUtils.downloadFile(voucherFile.fileBlob, voucherFile.fileName);
        return true;
      }
    });

    // キャッシュにない場合はAPIから取得
    if (!idExists) {
      this.fileService.fileDownload(purchaseVoucherNumber.toString()).subscribe(res => {
        const data = this.fileService.splitBlobAndFileName(res);
        this.voucherFileList.push({ id: purchaseVoucherNumber, fileName: data.fileName, fileBlob: data.blob });
        FileUtils.downloadFile(data.blob, data.fileName);
      }, () => this.fileDLErrorMessageCode = 'ERRORS.FILE_DL_ERROR');
    }
  }

  /**
   * スクロール時の処理
   * @param event ScrollEvent
   */
  onScroll(event: ScrollEvent): void {
    this.fileDLErrorMessageCode = null;
    if (event.isWindowEvent || !event.isReachingBottom) {
      return;
    }
    this.scrollToBottom.emit(event);
  }
}
//PRD_0133 #10181 add JFE end

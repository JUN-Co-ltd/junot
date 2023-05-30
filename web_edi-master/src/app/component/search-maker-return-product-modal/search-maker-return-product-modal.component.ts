import { Component, OnInit, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ScrollEvent } from 'ngx-scroll-event';
import { StringUtilsService } from 'src/app/service/bo/string-utils.service';
import { MakerReturnProductSearchConditions } from 'src/app/model/maker-return-product-search-conditions';
import { tap, finalize, catchError } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { MessageConfirmModalService } from 'src/app/service/message-confirm-modal.service';
import { MakerReturnProductComposite } from 'src/app/model/maker-return-product-composite';
import { MakerReturnProductsHttpService } from 'src/app/service/maker-return-products-http.service';
import { SeasonService } from 'src/app/service/bo/season.service';
import { TableUtilsService } from 'src/app/service/bo/table-utils.service';
import { MakerReturnService } from '../maker-return/service/maker-return.service';

@Component({
  selector: 'app-search-maker-return-product-modal',
  templateUrl: './search-maker-return-product-modal.component.html',
  styleUrls: ['./search-maker-return-product-modal.component.scss']
})
export class SearchMakerReturnProductModalComponent implements OnInit {

  /** 商品コード. */
  @Input()
  private inputProductCode: string = null;

  /** 店舗コード. */
  @Input()
  private inputShpcd: string = null;

  /** 仕入先. */
  @Input()
  supplier: { code: string, name: string } = { code: null, name: null };

  /** シーズンリスト. */
  seasons = this.seasonService.generateSeasonsFormValues();

  /** 検索フォーム. */
  searchCondition: MakerReturnProductSearchConditions = new MakerReturnProductSearchConditions();

  /** 検索結果. */
  searchResults: MakerReturnProductComposite[] = [];

  /** 選択行のindex */
  selectedIdx: number = null;

  /** 選択行のレコード */
  private selectedRecord: MakerReturnProductComposite = null;

  /** 次のページのトークン. */
  private nextPageToken: string = null;

  /** ローディング表示フラグ. */
  loading: boolean;

  constructor(
    public activeModal: NgbActiveModal,
    private stringUtils: StringUtilsService,
    private makerReturnProductsHttpService: MakerReturnProductsHttpService,
    private messageConfirmModalService: MessageConfirmModalService,
    private seasonService: SeasonService,
    private tableUtils: TableUtilsService,
    private makerReturnService: MakerReturnService
  ) { }

  ngOnInit() {
    this.initializeSearchCoondition();
    this.search();
  }

  /**
   * 検索条件を初期設定する.
   */
  private initializeSearchCoondition(): void {
    this.searchCondition.productCode = this.inputProductCode;
    this.searchCondition.shpcd = this.inputShpcd;
    this.searchCondition.supplierCode = this.supplier.code;
    // 商品コード未入力の場合は最新チェックtrue
    this.searchCondition.latestOrderOnly = this.stringUtils.isEmpty(this.inputProductCode);
  }

  /**
   * 検索条件に該当するデータを取得し、画面に結果を表示する。
   * @param pageToken ページトークン
   */
  private search(pageToken: string = null): void {
    this.loading = true;
    const searchConditions: MakerReturnProductSearchConditions = Object.assign({ ...this.searchCondition },
      {
        pageToken,
        subSeasonCodes: this.extractSelectedSeasons()
      } as MakerReturnProductSearchConditions);

    this.makerReturnProductsHttpService.search(searchConditions).pipe(
      tap(res => {
        this.searchResults = this.searchResults.concat(res.items);
        this.nextPageToken = res.nextPageToken;
      }),
      catchError(this.showErrorModal),
      finalize(() => this.loading = false)
    ).subscribe();
  }

  /**
   * 選択中のシーズンリストを取得する.
   * @returns 選択中のシーズンリスト
   */
  private extractSelectedSeasons(): string[] {
    return this.seasons.filter(season => season.selected).map(selected => selected.id.toString());
  }

  /**
   * 検索ボタンクリック時の処理。
   */
  onSearch(): void {
    this.searchResults = [];
    this.nextPageToken = null;
    this.search();
  }

  /**
   * スクロール時の処理.
   * @param event ScrollEvent
   */
  onScroll(event: ScrollEvent): void {
    if (this.tableUtils.cannotSearchNextPage(event, this.nextPageToken)) {
      return;
    }
    const pageToken = this.nextPageToken;
    this.nextPageToken = null; // すぐにnullにしないとイベント着火で2回呼ばれてしまう

    this.search(pageToken);
  }

  /**
   * 行選択時の処理.
   * 選択行のselectedのみtureを設定する.
   * @param selectedRecord 選択された行のデータ
   * @param selectedIdx 選択された行のインデックス
   */
  onSelectRow(selectedRecord: MakerReturnProductComposite, selectedIdx: number): void {
    this.selectedIdx = selectedIdx;
    this.selectedRecord = selectedRecord;
  }

  /**
   * 選択ボタンクリック時の処理.
   * 親ページに選択行のデータを渡し、モーダルを閉じる.
   */
  onSelectConfirm(): void {
    this.activeModal.close({
      makerReturnProductComposite: this.selectedRecord,
      productCode: this.makerReturnService.generateProductCode(this.selectedRecord)
    });
  }

  /**
   * エラーモーダルを表示する.
   * @param error エラー情報
   * @returns エラーモーダルの結果
   * - true : 「OK」が押された場合
   * - false : モーダルが閉じられた場合
   */
  private showErrorModal = (error: any): Observable<boolean> => this.messageConfirmModalService.openErrorModal(error);
}

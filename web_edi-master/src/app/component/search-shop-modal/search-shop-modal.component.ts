import { Component, OnInit, Input } from '@angular/core';
import { JunpcTnpmst } from 'src/app/model/junpc-tnpmst';
import { JunpcTnpmstSearchCondition } from 'src/app/model/junpc-tnpmst-search-condition';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { MessageConfirmModalService } from 'src/app/service/message-confirm-modal.service';
import { TableUtilsService } from 'src/app/service/bo/table-utils.service';
import { JunpcTnpmstHttpService } from 'src/app/service/junpc-tnpmst-http.service';
import { tap, catchError, finalize, map } from 'rxjs/operators';
import { ScrollEvent } from 'ngx-scroll-event';
import { Observable, forkJoin, of } from 'rxjs';
import { JunpcCodmstService } from 'src/app/service/junpc-codmst.service';
import { JunpcCodmst } from 'src/app/model/junpc-codmst';
import { Sort } from 'src/app/model/sort';
import { ShopKind } from 'src/app/const/shop-kind';

@Component({
  selector: 'app-search-shop-modal',
  templateUrl: './search-shop-modal.component.html',
  styleUrls: ['./search-shop-modal.component.scss']
})
export class SearchShopModalComponent implements OnInit {

  /** 店舗コード. */
  @Input()
  private inputShpcd: string = null;

  /** 店舗区分. */
  @Input()
  private inputShopkind: ShopKind = null;

  /** 事業部リスト */
  divisions$: Observable<JunpcCodmst[]> = null;

  /** 検索フォーム. */
  searchCondition: JunpcTnpmstSearchCondition = new JunpcTnpmstSearchCondition();

  /** 検索結果. */
  searchResults: JunpcTnpmst[] = [];

  /** 選択行のindex */
  selectedIdx: number = null;

  /** 選択行のレコード */
  private selectedRecord: JunpcTnpmst = null;

  /** 次のページのトークン. */
  private nextPageToken: string = null;

  /** ローディング表示フラグ. */
  loading: boolean;

  constructor(
    public activeModal: NgbActiveModal,
    private junpcCodmstService: JunpcCodmstService,
    private junpcTnpmstHttpService: JunpcTnpmstHttpService,
    private messageConfirmModalService: MessageConfirmModalService,
    private tableUtils: TableUtilsService,
  ) { }

  ngOnInit() {
    this.initializeSearchCoondition();
    const obs$ = forkJoin(
      this.fetchDivisions(),
      this.search(this.searchCondition)
    );
    this.loadingAndDoAsync(obs$);
  }

  /**
   * 検索条件を初期設定する.
   */
  private initializeSearchCoondition(): void {
    this.searchCondition.shpcd = this.inputShpcd;
    this.searchCondition.shopkind = this.inputShopkind;
    this.searchCondition.maxResults = 100;
    this.searchCondition.sort = { sortColumnName: 'shpcd' } as Sort;
  }

  /**
   * 事業部リストを取得する.
   * @returns Observable<void>
   */
  private fetchDivisions(): Observable<void> {
    this.divisions$ = this.junpcCodmstService.getAllDivisions().pipe(
      map(result => result.items));

    return of(null);
  }

  /**
   * 検索処理.
   * @param searchConditions 検索条件
   * @returns Observable<any>
   */
  private search(searchConditions: JunpcTnpmstSearchCondition): Observable<any> {
    return this.junpcTnpmstHttpService.search(searchConditions).pipe(
      tap(res => {
        this.searchResults = this.searchResults.concat(res.items);
        this.nextPageToken = res.nextPageToken;
      }));
  }

  /**
   * ローディングを表示して非同期処理を行う.
   * @param asyncFn 非同期処置
   */
  private loadingAndDoAsync(asyncFn: Observable<any>): void {
    this.loading = true;
    asyncFn.pipe(
      catchError(this.showErrorModal),
      finalize(() => this.loading = false)
    ).subscribe();
  }

  /**
   * 検索ボタンクリック時の処理。
   */
  onSearch(): void {
    this.searchResults = [];
    this.nextPageToken = null;
    this.loadingAndDoAsync(this.search(this.searchCondition));
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
    const searchCondition: JunpcTnpmstSearchCondition = Object.assign({ ...this.searchCondition }, { pageToken });

    this.loadingAndDoAsync(this.search(searchCondition));
  }

  /**
   * 行選択時の処理.
   * 選択行のselectedのみtureを設定する.
   * @param selectedRecord 選択された行のデータ
   * @param selectedIdx 選択された行のインデックス
   */
  onSelectRow(selectedRecord: JunpcTnpmst, selectedIdx: number): void {
    this.selectedIdx = selectedIdx;
    this.selectedRecord = selectedRecord;
  }

  /**
   * 選択ボタンクリック時の処理.
   * 親ページに選択行のデータを渡し、モーダルを閉じる.
   */
  onSelectConfirm(): void {
    this.activeModal.close(this.selectedRecord);
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

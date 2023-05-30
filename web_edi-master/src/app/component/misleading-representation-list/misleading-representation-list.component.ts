import { Component, OnInit } from '@angular/core';
import { FormControl, NgForm } from '@angular/forms';
import { ScrollEvent } from 'ngx-scroll-event';
import { Observable,  forkJoin, of } from 'rxjs';
import { tap, flatMap, catchError, finalize, filter, map } from 'rxjs/operators';

import { Path, EntireQualityApprovalType, EntireQualityApprovalDictionary, StorageKey, QualityApprovalStatus } from 'src/app/const/const';
import { HeaderService } from 'src/app/service/header.service';
import { JunpcCodmstService } from 'src/app/service/junpc-codmst.service';
import { ItemMisleadingRepresentationSearchResult } from 'src/app/model/item-misleading-representation-search-result';
import { MisleadingRepresentationViewSearchCondition } from 'src/app/model/misleading-representation-view-search-condition';
import { StringUtils } from 'src/app/util/string-utils';
import { MisleadingRepresentationService } from 'src/app/service/misleading-representation.service';
import { Authority } from 'src/app/enum/authority.enum';
import { SearchMethod } from 'src/app/enum/search-method.enum';
import { GenericList } from 'src/app/model/generic-list';
import { BrandCodesSearchConditions } from 'src/app/model/brand-codes-search-conditions';
import { JunpcCodmst } from 'src/app/model/junpc-codmst';
import { BrandCode } from 'src/app/model/brand-code';
import { LoadingService } from '../../service/loading.service';
import { MessageConfirmModalService } from 'src/app/service/message-confirm-modal.service';

import { Session } from '../../model/session';
import { SessionService } from '../../service/session.service';
import { BusinessCheckUtils } from 'src/app/util/business-check-utils';
import { DateUtils } from 'src/app/util/date-utils';
import { LocalStorageService } from 'src/app/service/local-storage.service';
import { CodeMaster } from 'src/app/const/code-master';

interface Season {
  code: string;
  selected: boolean;
}
interface Quality {
  type: number;
  label: string;
  selected: boolean;
}

@Component({
  selector: 'app-misleading-representation-list',
  templateUrl: './misleading-representation-list.component.html',
  styleUrls: ['./misleading-representation-list.component.scss']
})
export class MisleadingRepresentationListComponent implements OnInit {
  readonly PATH = Path;
  readonly AUTHORITY = Authority;

  private CSS_BLUE = 'font-color-blue';
  private CSS_RED = 'font-color-red';
  private CSS_GREEN = 'font-color-green';

  /** 画面に表示するメッセージ */
  message = {
    /** ボディ */
    body: {
      /** 異常系 */
      error: { code: '', param: null }
    }
  };
  /** 画面を非表示にする */
  invisibled = true;
  /** 検索結果 */
  searchResultItems: ItemMisleadingRepresentationSearchResult[] = [];
  /** 次のページのトークン */
  private nextPageToken: string;

  /** シーズンリスト(画面入力値保持用) */
  seasonList: Season[] = CodeMaster.seasonName.map(season => ({ code: season.code, selected: false }));
  /** 優良誤認リスト(画面入力値保持用) */
  qualityStatusList: Quality[] = [
    {
      type: EntireQualityApprovalType.ENTIRE_NON_INSPECTED,
      label: EntireQualityApprovalDictionary[EntireQualityApprovalType.ENTIRE_NON_INSPECTED],
      selected: false
    },
    {
      type: EntireQualityApprovalType.PART_INSPECTED,
      label: EntireQualityApprovalDictionary[EntireQualityApprovalType.PART_INSPECTED],
      selected: false
    },
    {
      type: EntireQualityApprovalType.ENTIRE_INSPECTED,
      label: EntireQualityApprovalDictionary[EntireQualityApprovalType.ENTIRE_INSPECTED],
      selected: false
    }
  ];

  /** 検索条件 */
  formConditions: MisleadingRepresentationViewSearchCondition = new MisleadingRepresentationViewSearchCondition();
  /** ブランドリスト */
  brandList: BrandCode[] = [];
  /** アイテムリスト */
  itemList: JunpcCodmst[] = [];

  /** ログインユーザのセッション */
  private session: Session;

  constructor(
    private headerSearvice: HeaderService,
    private misleadingRepresentationService: MisleadingRepresentationService,
    private junpcCodmstService: JunpcCodmstService,
    private sessionService: SessionService,
    private loadingService: LoadingService,
    private messageConfirmModalService: MessageConfirmModalService,
    private localStorageService: LocalStorageService
  ) { }

  ngOnInit() {
    this.headerSearvice.show();
    // セッション情報の取得
    this.session = this.sessionService.getSaveSession();

    let loadingToken = null;

    this.loadingService.start().pipe(
      tap((token) => loadingToken = token),
      tap(() => this.invisibled = true),
      tap(() => this.clearMessage()),
      flatMap(() =>
        forkJoin(
          this.getBrand(),
          this.getItem()
        )
      ),
      // Formにデータをセット
      tap(() => this.setStorageDataToForm(this.session, this.seasonList, this.qualityStatusList)),
      // 優良誤認対象を検索する
      flatMap(() => this.search()),
      tap(() => this.invisibled = false),
      // エラーがあればモーダル表示する
      catchError(error => {
        this.invisibled = true;
        return this.messageConfirmModalService.openErrorModal(error);
      }),
      // ローディング表示止める
      finalize(() => {
        this.loadingService.stop(loadingToken);
      })
      ).subscribe();
  }

  /**
   * ストレージに保存した入力値をFormに設定する.
   * @param session セッション
   * @param seasonList シーズンリスト
   * @param qualityStatusList 優良誤認リスト
   */
  private setStorageDataToForm(session: Session, seasonList: Season[], qualityStatusList: Quality[]): void {

    let storageObj = this.localStorageService.getSaveLocalStorage(session, StorageKey.STORAGE_KEY_MR_LIST_SEARCH_CONDITIONS);
    if (storageObj == null) {
      return null;
    }

    storageObj = JSON.parse(storageObj);

    this.formConditions = storageObj;
    const storageSeason = storageObj.subSeasonCodeList;
    const storageQuality = storageObj.qualityStatusList;

    // シーズンの初期値を設定
    const fnSeason = this.setSeasonSelected(seasonList);
    storageSeason.forEach(sesssionData => fnSeason(sesssionData));

    // 優良誤認状態の初期値を設定
    const fnSetQuality = this.setQualityStatusSelected(qualityStatusList);
    storageQuality.forEach(sesssionData => fnSetQuality(sesssionData));
  }

  /**
   * シーズンリストの選択状態を設定する.
   * @param seasonList シーズンリスト
   * @param sesssionData セッションデータ
   */
  private setSeasonSelected = (seasonList: Season[]) => (sesssionData: string): void =>
    seasonList.filter(season => season.code === sesssionData).forEach(season => season.selected = true)

  /**
   * 優良誤認リストの選択状態を設定する.
   * @param qualityStatusList 優良誤認リスト
   * @param sesssionData セッションデータ
   */
  private setQualityStatusSelected =
    (qualityStatusList: Quality[]) => (sesssionData: number): void =>
      qualityStatusList.filter(qualityStatus => qualityStatus.type === sesssionData).forEach(qualityStatus => qualityStatus.selected = true)

  /**
   * 検索ボタン押下時の処理
   * @param searchForm
   */
  onSearch(searchForm: NgForm): void {

    let loadingToken = null;

    // ローディング開始
    this.loadingService.start().pipe(
      // ローディングトークン退避
      tap((token) => loadingToken = token),
      filter(() => this.isValid(searchForm)),
      tap(() => this.clearMessage()),
      filter(() => this.isValid(searchForm)),
      // 検索前に前の検索結果を初期化
      tap(() => this.searchResultItems = []),
      flatMap(() => this.search()),
      catchError((error) => this.messageConfirmModalService.openErrorModal(error)),
      finalize(() => this.loadingService.stop(loadingToken))
    ).subscribe();
  }

  /**
   * 検索処理を行う
   * @returns 検索結果
   */
  private search(): Observable<GenericList<ItemMisleadingRepresentationSearchResult>> {
    // 優良誤認一覧を検索
    return this.misleadingRepresentationService.search(this.generateApiSearchCondition()).pipe(
      tap(genericList => {
        // 次のページのトークンを保存
        this.nextPageToken = genericList.nextPageToken;
        // もし1件もなければ、メッセージを表示
        if (genericList.items.length === 0) {
          this.message.body.error = { code: 'INFO.RESULT_ZERO', param: null };
        } else {
          // 検索結果が1件以上の場合は一覧に表示
          this.searchResultItems = genericList.items;
        }
      }),
    );
  }

  /**
   * API検索用Modelを設定する
   * @returns 値を設定したModel
   */
  private generateApiSearchCondition(): MisleadingRepresentationViewSearchCondition {

    const searchCondition = Object.assign({}, { ...this.formConditions });

    // シーズン
    searchCondition.subSeasonCodeList = this.selectedSeason();
    // 承認ステータス
    searchCondition.qualityStatusList = this.selectedQualityStatus();
    // 検索方法（全てAND条件）
    searchCondition.searchMethod = SearchMethod.ALL_AND_FULL;

    // Sessionに条件を保存
    this.saveFormConditions();

    return searchCondition;
  }

  /**
   * スクロール時、最下部までスクロールされた場合は、次の検索結果を取得する
   * @param event ScrollEvent
   */
  onScroll(event: ScrollEvent): void {
    if (this.isNotReadNext(event)) {
      return;
    }
    let loadingToken = null;
    // ローディング表示開始（画面非活性）
    this.loadingService.start().pipe(
      // ローディングトークン退避
      tap((token) => loadingToken = token),
      // 優良誤認一覧検索
      flatMap(() => this.nextSearch()),
      // エラーが発生した場合はエラーモーダルを表示
      catchError((error) => this.messageConfirmModalService.openErrorModal(error)),
      // ローディング終了
      finalize(() => this.loadingService.stop(loadingToken))
    ).subscribe();
  }

  /**
   * 次の100件を呼ばないかを判断する
   * @param event スクロールイベント
   * @returns true: 次の100件を呼ばない
   */
  private isNotReadNext(event: ScrollEvent): boolean {
    // isWindowEventは外側のスクロールが動いたかどうか
    return !event.isWindowEvent && !event.isReachingBottom || StringUtils.isEmpty(this.nextPageToken);
  }

  /**
   * 次の検索処理を行う
   * @returns 検索結果
   */
  private nextSearch(): Observable<GenericList<ItemMisleadingRepresentationSearchResult>> {
    const nextPageToken = this.nextPageToken;
    this.nextPageToken = null;
    return this.misleadingRepresentationService.search({ pageToken: nextPageToken } as MisleadingRepresentationViewSearchCondition).pipe(
      tap(genericList => {
        this.nextPageToken = genericList.nextPageToken;
        this.searchResultItems = this.searchResultItems.concat(genericList.items);
      })
    );
  }

  /**
   * 選択中のシーズンを取得する.
   * @returns 選択中のシーズンリスト
   */
  private selectedSeason(): string[] {
    return this.seasonList.filter(season => season.selected).map(selected => selected.code);
  }

  /**
   * 選択中の優良誤認ステータスを取得する.
   * @returns 選択中の優良誤認ステータスリスト
   */
  private selectedQualityStatus(): number[] {
    return this.qualityStatusList.filter(qualityStatus => qualityStatus.selected).map(qualityStatus => qualityStatus.type);
  }

  /**
   * バリデーションの結果を取得する。バリデーションエラーがある場合、フッターにバリデーションエラーメッセージを表示する。
   * @returns バリデーションの結果
   * - true : 正常
   * - false : エラーあり
   */
  private isValid(searchForm: NgForm): boolean {

    if (searchForm == null) {
      return true;
    }

    if (searchForm.invalid) {
      return false; // validationエラーがある場合終了
    }
    return true;
  }

  /**
   * ブランドコードリストを取得するAPIコール
   * @retruns ブランドコードリスト
   */
  private getBrand = (): Observable<GenericList<BrandCode>> =>
    this.junpcCodmstService.getBrandCodes(new BrandCodesSearchConditions()).pipe(
      tap(data => this.brandList = data.items)
    )

  /**
   * アイテムコードリストを取得する
   * @returns アイテムコードリスト
   */
  private getItem = (): Observable<GenericList<JunpcCodmst>> =>
    this.junpcCodmstService.getItems().pipe(
      tap(data => this.itemList = data.items)
    )

  /**
   * エラーがあるかどうかを返却する
   * @param value FormControl
   * @returns エラーがある場合にtrueを返す
   */
  isErrorDisplay(value: FormControl): boolean {
    if (value == null) {
      return false;
    }
    return value.invalid && (value.dirty || value.touched);
  }

  /**
   * メッセージをクリアする
   */
  private clearMessage(): void {
    this.message = {
      body: {
        error: { code: '', param: null }
      }
    };
  }

  /**
   * 検索条件をstorageに保持.
   */
  private saveFormConditions(): void {
    this.formConditions.subSeasonCodeList = this.selectedSeason();
    this.formConditions.qualityStatusList = this.selectedQualityStatus();
    this.localStorageService.createLocalStorage(this.session, StorageKey.STORAGE_KEY_MR_LIST_SEARCH_CONDITIONS, this.formConditions);
  }

  /**
   * 国・組成・有害の優良誤認ステータスによって品番の色を変える
   * @param data 優良誤認一覧データ
   * @returns フォントカラー
   */
  fontColorChanged(data: ItemMisleadingRepresentationSearchResult): string {
    // 承認済
    if (BusinessCheckUtils.isQualityApprovalOk(data)) {
      return this.CSS_BLUE;
    }
    // 進捗なし
    if (BusinessCheckUtils.isQualityApprovalNoProgress(data)) {
      return this.CSS_RED;
    }
    // 一部承認
    return this.CSS_GREEN;
  }

  /**
   * 優良誤認の状態に応じたフォントカラーのCSS設定を返却する
   * @param status 優良誤認状態
   * @returns フォントカラー
   */
  fontColorStatus(status: number): string {

    switch (status) {
      case QualityApprovalStatus.TARGET:
        return this.CSS_RED;
      case QualityApprovalStatus.PART:
        return this.CSS_GREEN;
      case QualityApprovalStatus.ACCEPT:
        return this.CSS_BLUE;
      default:
        return null;
    }
  }

  /**
   * 日付フォーカスアウト時処理.
   * @param maskedValue 変換済の値
   * @param type 日付項目名
   */
  onBlurDate(maskedValue: string, type: string): void {
    // pipeの変換値がformにセットされないので別途格納
    const ngbDate = DateUtils.onBlurDate(maskedValue);
    if (ngbDate) { this.formConditions[type] = ngbDate; }
  }
}

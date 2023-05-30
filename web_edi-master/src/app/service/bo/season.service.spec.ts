import { TestBed } from '@angular/core/testing';
import { SeasonService } from './season.service';
import { NumberUtilsService } from './number-utils.service';

describe('SeasonService', () => {
  let seasonService: SeasonService;

  // let numberUtilsSpy: any;
  beforeEach(() => {
    // numberUtilsSpy = jasmine.createSpyObj('NumberUtilsService', ['defaultNull']);

    TestBed.configureTestingModule({
      providers: [
        SeasonService,
        NumberUtilsService // spyにすべき？
        // { provide: NumberUtilsService, useValue: numberUtilsSpy }
      ]
    });

    seasonService = TestBed.get(SeasonService);
  });

  /**
   * findSubSeasonValue
   * 指定したidに該当するシーズン名を返す.
   */
  it('should findSubSeasonValue return subSeasonName', () => {
    // 準備
    // numberUtilsSpy.defaultNull.and.returnValues(1);

    // 実行
    const result = seasonService.findSubSeasonValue(1);

    // 確認
    expect(result).toBe('A1');
    // expect(numberUtilsSpy.defaultNull).toHaveBeenCalledTimes(1);
  });

  /**
   * findSubSeasonValue
   * 指定したidに該当するシーズンがない場合、nullを返す.
   */
  it('should findSubSeasonValue return null if not exists', () => {
    // 準備
    // numberUtilsSpy.defaultNull.and.returnValues(3);

    // 実行
    const result = seasonService.findSubSeasonValue(3);

    // 確認
    expect(result).toBe(null);
    // expect(numberUtilsSpy.defaultNull).toHaveBeenCalledTimes(1);
  });
});

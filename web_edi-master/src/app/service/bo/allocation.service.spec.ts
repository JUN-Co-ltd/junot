import { TestBed } from '@angular/core/testing';
import { ALLOCATIONS } from './mocks/allocation.service.mock';
import { AllocationService } from './allocation.service';

describe('AllocationService', () => {
  let allocationService: AllocationService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AllocationService]
    });

    allocationService = TestBed.get(AllocationService);
  });

  /**
   * findAllocationName
   * 配分課名を返す.
   */
  it('should findAllocationName return allocationName', () => {
    // 実行
    const result = allocationService.findAllocationName(ALLOCATIONS, '18');

    // 確認
    expect(result).toBe('配分課名４', 'allocationName is unexpected');
  });

  /**
   * findAllocationName
   * nullを返す.
   */
  it('should findAllocationName return null', () => {
    // 実行
    const result = allocationService.findAllocationName(ALLOCATIONS, '99');

    // 確認
    expect(result).toBe(null, 'allocationName is not null');
  });
});

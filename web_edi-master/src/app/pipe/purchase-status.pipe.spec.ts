import { PurchaseStatusPipe } from './purchase-status.pipe';

describe('PurchaseStatusPipe', () => {
  it('create an instance', () => {
    const pipe = new PurchaseStatusPipe();
    expect(pipe).toBeTruthy();
  });
});

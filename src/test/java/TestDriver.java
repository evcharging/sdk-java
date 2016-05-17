import com.evchargings.union.*;

public class TestDriver implements UnionDriver {
    Charge charge;
    int ngets;
    @Override
    public void init(Union union) {
    }

    @Override
    public Station[] getStations(int code, int page, int pageSize) {
        return new Station[0];
    }

    @Override
    public Pile[] getPiles(String outId) {
        return new Pile[0];
    }

    @Override
    public void startCharging(String outId, long orderId) throws UnionException {
        charge = new Charge();
        charge.setId(System.currentTimeMillis());
        charge.setOutId(Long.toString(orderId));
        charge.setState(Charge.State.BUSY);
        charge.setStarted(System.currentTimeMillis());
        charge.setEnergy(0);
        charge.setCost(0);
        charge.setOutPileId(outId);
        charge.setOutUserId("");
        ngets = 0;
    }

    @Override
    public Charge stopCharging(String outId) throws UnionException {
        charge.setState(Charge.State.DONE);
        Charge c = charge;
        ngets = 0;
        this.charge = null;
        return c;
    }

    @Override
    public Charge getCharging(String outId) throws UnionException {
        ngets++;
        if (ngets == 3)
            charge = stopCharging(outId);
        charge.setEnergy(ngets);
        charge.setCost(ngets);
        return charge;
    }

    @Override
    public void onChargeChange(Charge charge) {

    }
}

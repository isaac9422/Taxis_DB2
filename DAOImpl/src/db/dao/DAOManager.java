
package db.dao;

/**
 *
 * @author Jhunior
 */
public class DAOManager {
    private HoyoDAOImpl hoyoDAOImpl;
    private CarroDAOImpl carroDAOImpl;
    
    public DAOManager(){
        this.carroDAOImpl = new CarroDAOImpl();
        this.hoyoDAOImpl = new HoyoDAOImpl();
    }

    /**
     * @return the hoyoDAOImpl
     */
    public HoyoDAOImpl getHoyoDAOImpl() {
        return hoyoDAOImpl;
    }

    /**
     * @param hoyoDAOImpl the hoyoDAOImpl to set
     */
    public void setHoyoDAOImpl(HoyoDAOImpl hoyoDAOImpl) {
        this.hoyoDAOImpl = hoyoDAOImpl;
    }

    /**
     * @return the carroDAOImpl
     */
    public CarroDAOImpl getCarroDAOImpl() {
        return carroDAOImpl;
    }

    /**
     * @param carroDAOImpl the carroDAOImpl to set
     */
    public void setCarroDAOImpl(CarroDAOImpl carroDAOImpl) {
        this.carroDAOImpl = carroDAOImpl;
    }
}
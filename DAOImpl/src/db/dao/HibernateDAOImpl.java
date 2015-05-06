/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package db.dao;


import java.io.Serializable;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * 
 * @param <T>
 *            Objeto del modelo
 * @param <ID>
 *            clavePrimaria
 */
public class HibernateDAOImpl<T, ID extends Serializable> implements
		HibernateDAO<T, ID> {

	public static final ThreadLocal<Session> SESSION = new ThreadLocal<Session>();
	private static final SessionFactory sessionFactory;
	private Class<T> type;

	/**
	 * Se obtiene la session de hibernate, directamente del bean spring
	 */
	static {
		ApplicationContext factory = new ClassPathXmlApplicationContext(
				"SpringConfiguration.xml");
		sessionFactory = (SessionFactory) factory.getBean("sessionFactory");
	}

	/**
	 * Constructor
	 * 
	 * @param type tipo class
	 */
	public HibernateDAOImpl(Class<T> type) {
		this.type = type;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ID guardar(T instancia) {
		// getCurrentSession().beginTransaction();
		return (ID) getCurrentSession().save(instancia);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T cargar(ID clavePrimaria) {
		// getCurrentSession().beginTransaction();
		return (T) getCurrentSession().get(type, clavePrimaria);
	}

	@Override
	public void actualizar(T instancia) {
		// getCurrentSession().beginTransaction();
		getCurrentSession().update(instancia);

	}

	@Override
	public void eliminar(T instancia) {
		// getCurrentSession().beginTransaction();
		getCurrentSession().delete(instancia);

	}

	/**
	 * Obtiene la conexion actual con la base de datos
	 * 
	 * @return
	 */
	public Session getCurrentSession() {
		Session sess = SESSION.get();
		if (sess == null) {
			sess = sessionFactory.openSession();
			SESSION.set(sess);
		} else if (!sess.isOpen()) {
			sess = sessionFactory.openSession();
			SESSION.set(sess);
		}
		return sess;
	}

	public void iniciarTransaccion() {
		getCurrentSession().beginTransaction();
	}

	/**
	 * Ejecuta la transaccion
	 */
	public void commit() {
		getCurrentSession().getTransaction().commit();

	}

	public void flush() {
		getCurrentSession().flush();
	}

	/**
	 * Cierra la conexion actual con la base de datos
	 */
	public void cerrarSession() {
		Session s = SESSION.get();
		SESSION.set(null);
		if (s != null) {
			s.close();
		}
	}

	@Override
	public List<T> listar() {
		return findByCriteria();

	}
        
	@SuppressWarnings("unchecked")
        @Override
	public List<T> listarConLimite(int limiteResultados) {
            Criteria crit = getCurrentSession().createCriteria(type);
            crit.setMaxResults(limiteResultados);
            return crit.list();
	}

	@SuppressWarnings("unchecked")
	/**
	 * Consulta generica que retorna una lista de elementos dado un conjunto de 
	 * restricciones
	 * @param criterion restricciones 
	 * @return
	 */
	protected List<T> findByCriteria(Criterion... criterion) {
		// getCurrentSession().beginTransaction();
		Criteria crit = getCurrentSession().createCriteria(type);
                
		for (Criterion c : criterion) {
			crit.add(c);
		}

		return crit.list();
	}

	@SuppressWarnings("unchecked")
	/**
	 * Consulta generica que retorna un unico elemento dado un conjunto de 
	 * restricciones
	 * @param criterion restricciones 
	 * @return
	 */
	protected T findByAttributes(Criterion... criterion) {
		Criteria crit = getCurrentSession().createCriteria(type);
		for (Criterion c : criterion) {
			crit.add(c);
		}
		return (T) crit.uniqueResult();
	}
}
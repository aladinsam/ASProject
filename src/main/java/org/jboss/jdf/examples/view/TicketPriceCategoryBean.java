package org.jboss.jdf.examples.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateful;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.jboss.jdf.example.ticketmonster.model.TicketPriceCategory;
import org.jboss.jdf.example.ticketmonster.model.Section;
import org.jboss.jdf.example.ticketmonster.model.Show;
import org.jboss.jdf.example.ticketmonster.model.TicketCategory;

/**
 * Backing bean for TicketPriceCategory entities.
 * <p>
 * This class provides CRUD functionality for all TicketPriceCategory entities. It focuses
 * purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt> for
 * state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD framework or
 * custom base class.
 */

@Named
@Stateful
@ConversationScoped
public class TicketPriceCategoryBean implements Serializable {

	private static final long serialVersionUID = 1L;

	/*
	 * Support creating and retrieving TicketPriceCategory entities
	 */

	private Long id;

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	private TicketPriceCategory ticketPriceCategory;

	public TicketPriceCategory getTicketPriceCategory() {
		return this.ticketPriceCategory;
	}

	@Inject
	private Conversation conversation;

	@PersistenceContext(type = PersistenceContextType.EXTENDED)
	private EntityManager entityManager;

	public String create() {

		this.conversation.begin();
		return "create?faces-redirect=true";
	}
	
	public void retrieve() {

		if (FacesContext.getCurrentInstance().isPostback()) {
			return;
		}

		if (this.conversation.isTransient()) {
			this.conversation.begin();
		}

		if (this.id == null) {
			this.ticketPriceCategory = this.search;
		} else {
			this.ticketPriceCategory = this.entityManager.find(TicketPriceCategory.class, getId());
		}
	}

	/*
	 * Support updating and deleting TicketPriceCategory entities
	 */

	public String update() {
		this.conversation.end();
		
		try {
			if (this.id == null) {
				this.entityManager.persist(this.ticketPriceCategory);
				return "search?faces-redirect=true";			
			} else {
				this.entityManager.merge(this.ticketPriceCategory);
				return "view?faces-redirect=true&id=" + this.ticketPriceCategory.getId();
			}
		} catch( Exception e ) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage( e.getMessage() ));
			return null;
		}
	}

	public String delete() {
		this.conversation.end();

		try {
			this.entityManager.remove(this.entityManager.find(TicketPriceCategory.class,
					getId()));
			this.entityManager.flush();
			return "search?faces-redirect=true";
		} catch( Exception e ) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage( e.getMessage() ));
			return null;
		}
	}

	/*
	 * Support searching TicketPriceCategory entities with pagination
	 */

	private int page;
	private long count;
	private List<TicketPriceCategory> pageItems;
	
	private TicketPriceCategory search = new TicketPriceCategory();

	public int getPage() {
		return this.page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPageSize() {
		return 10;
	}

	public TicketPriceCategory getSearch() {
		return this.search;
	}

	public void setSearch(TicketPriceCategory search) {
		this.search = search;
	}

	public void search() {
		this.page = 0;
	}

	public void paginate() {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();

		// Populate this.count

		CriteriaQuery<Long> countCriteria = builder.createQuery(Long.class);
		Root<TicketPriceCategory> root = countCriteria.from(TicketPriceCategory.class);
		countCriteria = countCriteria.select(builder.count(root)).where(
				getSearchPredicates(root));
		this.count = this.entityManager.createQuery(countCriteria)
				.getSingleResult();

		// Populate this.pageItems

		CriteriaQuery<TicketPriceCategory> criteria = builder.createQuery(TicketPriceCategory.class);
		root = criteria.from(TicketPriceCategory.class);
		TypedQuery<TicketPriceCategory> query = this.entityManager.createQuery(criteria
				.select(root).where(getSearchPredicates(root)));
		query.setFirstResult(this.page * getPageSize()).setMaxResults(
				getPageSize());
		this.pageItems = query.getResultList();
	}

	private Predicate[] getSearchPredicates(Root<TicketPriceCategory> root) {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
		List<Predicate> predicatesList = new ArrayList<Predicate>();

		Show show = this.search.getShow();
		if (show != null) {
			predicatesList.add(builder.equal(root.get("show"), show));
		}
		Section section = this.search.getSection();
		if (section != null) {
			predicatesList.add(builder.equal(root.get("section"), section));
		}
		TicketCategory ticketCategory = this.search.getTicketCategory();
		if (ticketCategory != null) {
			predicatesList.add(builder.equal(root.get("ticketCategory"), ticketCategory));
		}

		return predicatesList.toArray(new Predicate[predicatesList.size()]);
	}

	public List<TicketPriceCategory> getPageItems() {
		return this.pageItems;
	}

	public long getCount() {
		return this.count;
	}

	/*
	 * Support listing and POSTing back TicketPriceCategory entities (e.g. from inside an
	 * HtmlSelectOneMenu)
	 */

	public List<TicketPriceCategory> getAll() {

		CriteriaQuery<TicketPriceCategory> criteria = this.entityManager
				.getCriteriaBuilder().createQuery(TicketPriceCategory.class);
		return this.entityManager.createQuery(
				criteria.select(criteria.from(TicketPriceCategory.class))).getResultList();
	}

	public Converter getConverter() {

		return new Converter() {

			@Override
			public Object getAsObject(FacesContext context,
					UIComponent component, String value) {

				return TicketPriceCategoryBean.this.entityManager.find(TicketPriceCategory.class,
						Long.valueOf(value));
			}

			@Override
			public String getAsString(FacesContext context,
					UIComponent component, Object value) {

				if (value == null) {
					return "";
				}

				return String.valueOf(((TicketPriceCategory) value).getId());
			}
		};
	}
	
	/*
	 * Support adding children to bidirectional, one-to-many tables
	 */
	 
	private TicketPriceCategory add = new TicketPriceCategory();

	public TicketPriceCategory getAdd() {
		return this.add;
	}

	public TicketPriceCategory getAdded() {
		TicketPriceCategory added = this.add;
		this.add = new TicketPriceCategory();
		return added;
	}
}
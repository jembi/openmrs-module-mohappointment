/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.mohappointment.db.hibernate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Concept;
import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.mohappointment.db.AppointmentDAO;
import org.openmrs.module.mohappointment.model.MohAppointment;
import org.openmrs.module.mohappointment.model.AppointmentState;
import org.openmrs.module.mohappointment.model.ServiceProviders;
import org.openmrs.module.mohappointment.model.Services;
import org.openmrs.module.mohappointment.singletonpattern.AppointmentList;

/**
 * @author Kamonyo
 * 
 *         This is the Appointment Services working together with the Hibernate
 */

@SuppressWarnings("unchecked")
public class HibernateAppointmentDAO implements AppointmentDAO {

	private DbSessionFactory sessionFactory;

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * @param sessionFactory
	 *            the sessionFactory to set
	 */
	public void setSessionFactory(DbSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * @return the sessionFactory
	 */
	public DbSessionFactory getSessionFactory() {
		return sessionFactory;
	}

	/**
	 * @see org.openmrs.module.appointment.db.AppointmentDAO#getAllAppointments()
	 */

	@Override
	public Collection<MohAppointment> getAllAppointments() {

		DbSession session = sessionFactory.getCurrentSession();

		Collection<MohAppointment> appointments = session.createCriteria(
				MohAppointment.class).list();
		// Collection<Appointment> appointments = session.createSQLQuery(
		// "SELECT * FROM appointment").list();

		return appointments;
	}

	/**
	 * @see org.openmrs.module.appointment.db.AppointmentDAO#saveAppointment(org.openmrs.module.appointment.IAppointment)
	 */
	@Override
	public Integer lastAppointmentId() {
		Integer lastId = 0;

		DbSession session = sessionFactory.getCurrentSession();
		lastId = (Integer) session
				.createSQLQuery(
						"SELECT appointment_id FROM moh_appointment ORDER BY appointment_id DESC LIMIT 1;")
				.uniqueResult();

		return lastId;
	}

	/**
	 * @see org.openmrs.module.appointment.db.AppointmentDAO#saveAppointment(org.openmrs.module.appointment.IAppointment)
	 */
	@Override
	public void saveAppointment(MohAppointment appointment) {
		DbSession session = sessionFactory.getCurrentSession();
		session.saveOrUpdate(appointment);
	}

	/**
	 * @see org.openmrs.module.appointment.db.AppointmentDAO#updateAppointment(org.openmrs.module.appointment.IAppointment)
	 */
	@Override
	public void updateAppointment(MohAppointment appointment) {
		DbSession session = sessionFactory.getCurrentSession();
		session.saveOrUpdate(appointment);
	}

	@Override
	public void updateState(MohAppointment appointment, Integer stateId) {
		DbSession session = sessionFactory.getCurrentSession();
		session.createSQLQuery(
				"UPDATE moh_appointment SET appointment_state_id = " + stateId
						+ " WHERE appointment_id = "
						+ appointment.getAppointmentId() + ";").executeUpdate();

		log.info("________________________________________"
				+ "UPDATE moh_appointment SET appointment_state_id = "
				+ stateId + " WHERE appointment_id = "
				+ appointment.getAppointmentId() + ";");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.db.AppointmentDAO#cancelAppointment
	 * (org.openmrs.module.mohappointment.service.IAppointment)
	 */
	@Override
	public void cancelAppointment(MohAppointment appointment) {

		DbSession session = sessionFactory.getCurrentSession();
		appointment.setVoided(true);
		session.update(appointment);
	}

	/**
	 * @see org.openmrs.module.appointment.db.AppointmentDAO#getAppointmentById(int)
	 */

	@Override
	public MohAppointment getAppointmentById(int appointmentId) {

		DbSession session = sessionFactory.getCurrentSession();

		MohAppointment appointment = (MohAppointment) session.load(MohAppointment.class,
				appointmentId);

		return appointment;
	}

	/**
	 * @see org.openmrs.module.appointment.db.AppointmentDAO#getAppointmentsByMulti(java.lang.Object[])
	 */

	@Override
	public List<Integer> getAppointmentIdsByMulti(Object[] conditions, int limit) {

		DbSession session = sessionFactory.getCurrentSession();
		List<Integer> appointmentIds;

		StringBuilder combinedSearch = new StringBuilder("");

		if (conditions != null) {// Returns combined conditions matching
			// appointments.

			combinedSearch
					.append("SELECT DISTINCT appointment_id FROM moh_appointment WHERE ");

			if (null != conditions[0] && !conditions[0].equals(""))
				combinedSearch.append(" patient_id = " + conditions[0]
						+ " AND ");
			if (null != conditions[1])

				if (Context.getUserService().getUser(
						Integer.valueOf("" + conditions[1])) != null)
					if (!conditions[1].equals("")
							&& Context
									.getUserService()
									.getUser(
											Integer.valueOf("" + conditions[1]))
									.getPerson() != null)
						// Code has to be inserted here in order to load those
						// appointment depending on the provider working in this
						// service
						combinedSearch.append(" provider_id = "
								+ Context
										.getUserService()
										.getUser(
												Integer.valueOf(""
														+ conditions[1]))
										.getPerson().getPersonId() + " AND ");

			if (null != conditions[2] && !conditions[2].equals(""))
				combinedSearch.append(" location_id = " + conditions[2] + ""
						+ " AND ");
			if (null != conditions[3] && !conditions[3].equals("")) {
				combinedSearch.append(" appointment_date >= '"
						+ new SimpleDateFormat("yyyy-MM-dd")
								.format((Date) conditions[3]) + "' AND ");
			}
			if (null != conditions[4] && !conditions[4].equals("")) {
				if ((Boolean) conditions[4] == false)
					combinedSearch.append(" attended = "
							+ (Boolean) conditions[4] + " AND ");
				else
					// Here we need both Attended and Non-Attended when Include
					// Attended is selected
					combinedSearch
							.append(" (attended = TRUE OR attended = FALSE) AND ");
			}
			if (null != conditions[5] && !conditions[5].equals(""))
				combinedSearch.append(" appointment_date <= '"
						+ new SimpleDateFormat("yyyy-MM-dd")
								.format((Date) conditions[5]) + "' AND ");

			if (null != conditions[6] && !conditions[6].equals(""))
				combinedSearch.append(" appointment_state_id = "
						+ conditions[6] + " AND ");

			// Another condition object for services related to the
			// provider.
			if (null != conditions[7] && !conditions[7].equals(""))
				combinedSearch.append(" service_id = " + conditions[7]
						+ " AND ");

			// if (null != conditions[8] && !conditions[8].equals(""))
			// combinedSearch.append(" reason_obs_id = " + conditions[8] +
			// " AND ");

			combinedSearch
					.append(" voided = false ORDER BY appointment_date DESC LIMIT "
							+ limit + ";");

			appointmentIds = session.createSQLQuery(combinedSearch.toString())
					.list();

			log.info("________________>>>>> " + combinedSearch.toString());

		} else {
			// Returns all future appointment not yet attended when nothing or
			// no conditions selected.
			appointmentIds = session
					.createSQLQuery(
							"SELECT appointment_id FROM moh_appointment WHERE attended = false AND voided = false AND appointment_date >= CURDATE() LIMIT 50")
					.list();
		}

		return appointmentIds;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.db.AppointmentDAO#loadAllAppointments()
	 */
	@Override
	public void loadAllAppointments() {

		DbSession session = sessionFactory.getCurrentSession();
		Collection<MohAppointment> appointments = session
				.createSQLQuery(
						"select app.* from moh_appointment app where voided = false;")
				.addEntity("app", MohAppointment.class).list();

		if (appointments != null)
			if (appointments.size() == 0) {
				for (MohAppointment appoint : appointments) {

					AppointmentList.getInstance().addAppointment(appoint);

				}
			}
	}

	// ************* AppointmentState DB Code *************

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.db.AppointmentDAO#getAppointmentStates
	 * ()
	 */
	@Override
	public Collection<AppointmentState> getAppointmentStates() {
		DbSession session = getSessionFactory().getCurrentSession();
		List<AppointmentState> states = session.createCriteria(
				AppointmentState.class).list();
		return states;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.openmrs.module.mohappointment.db.AppointmentDAO#
	 * getAppointmentStatesByName(java.lang.String)
	 */
	@Override
	public AppointmentState getAppointmentStatesByName(String name) {
		DbSession session = getSessionFactory().getCurrentSession();

		Object[] appState = (Object[]) session
				.createSQLQuery(
						"SELECT appointment_state_id, description FROM moh_appointment_state WHERE description = '"
								+ name + "';").uniqueResult();

		AppointmentState appointmentState = new AppointmentState(
				(Integer) appState[0], (String) appState[1]);

		System.out.println(">>>>>>>>>>> Matched state: "
				+ appointmentState.toString());

		return appointmentState;
	}

	// ************* Services and ServiceProviders DB Code *************

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.db.AppointmentDAO#saveService(org.openmrs
	 * .module.mohappointment.service.Services)
	 */
	@Override
	public void saveService(Services service) {
		DbSession session = sessionFactory.getCurrentSession();
		session.save(service);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.db.AppointmentDAO#saveServiceProviders
	 * (org.openmrs.module.mohappointment.service.IServiceProviders)
	 */
	@Override
	public void saveServiceProviders(ServiceProviders serviceProvider) {
		DbSession session = sessionFactory.getCurrentSession();
		session.saveOrUpdate(serviceProvider);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.db.AppointmentDAO#updateService(org
	 * .openmrs.module.mohappointment.service.Services)
	 */
	@Override
	public void updateService(Services service) {
		saveService(service);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.db.AppointmentDAO#updateServiceProviders
	 * (org.openmrs.module.mohappointment.service.IServiceProviders)
	 */
	@Override
	public void updateServiceProviders(ServiceProviders serviceProvider) {
		saveServiceProviders(serviceProvider);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.db.AppointmentDAO#getPersonsByService
	 * (org.openmrs.module.mohappointment.service.Services)
	 */
	@Override
	public Collection<Integer> getPersonsByService(Services service) {
		DbSession session = sessionFactory.getCurrentSession();

		Collection<Integer> providers = session.createSQLQuery(
				"SELECT provider FROM moh_appointment_service_providers WHERE service = "
						+ service.getServiceId()).list();

		return providers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.db.AppointmentDAO#getServiceByProvider
	 * (org.openmrs.Person)
	 */
	@Override
	public Services getServiceByProvider(Person provider) {
		DbSession session = sessionFactory.getCurrentSession();

		if (provider != null) {
			ServiceProviders sp = (ServiceProviders) session
					.createCriteria(ServiceProviders.class)
					.add(Restrictions.eq("provider", provider)).uniqueResult();
			try {
				return sp.getService();
			} catch (NullPointerException npe) {
				return null;
			}
		} else
			return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.db.AppointmentDAO#getServiceById(java
	 * .lang.Integer)
	 */
	@Override
	public Services getServiceById(Integer serviceId) {
		DbSession session = sessionFactory.getCurrentSession();
	
		Services services = (Services) session.load(Services.class, serviceId);

		return services;
	}
	
	@Override
	public ServiceProviders getServiceProviderById(int serviceProviderId) {
		DbSession session = sessionFactory.getCurrentSession();

		ServiceProviders serviceProvider = (ServiceProviders) session.load(ServiceProviders.class, serviceProviderId);

		return serviceProvider;
	}

	/**
	 * @see org.openmrs.module.mohappointment.db.AppointmentDAO#getServiceByConcept(org.openmrs.Concept)
	 */
	@Override
	public Services getServiceByConcept(Concept concept) {
		DbSession session = sessionFactory.getCurrentSession();

		Services service = (Services) session.createCriteria(Services.class)
				.add(Restrictions.eq("concept", concept)).uniqueResult();

		return service;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.db.AppointmentDAO#getServiceProviders()
	 */
	@Override
	public Collection<ServiceProviders> getServiceProviders() {
		DbSession session = sessionFactory.getCurrentSession();

		// Collection<IServiceProviders> serviceProviders = session
		// .createSQLQuery(
		// "SELECT * FROM service_providers WHERE voided  = FALSE")
		// .list();
		Collection<ServiceProviders> serviceProviders = session.createCriteria(
				ServiceProviders.class).list();

		return serviceProviders;
	}

	/**
	 * (non-Jsdoc)
	 * 
	 * @see org.openmrs.module.mohappointment.db.AppointmentDAO#getServicesByProvider(org.openmrs.Person)
	 */
	@Override
	public Collection<Services> getServicesByProvider(Person provider) {
		DbSession session = sessionFactory.getCurrentSession();
		List<Services> services = new ArrayList<Services>();

		if (provider != null) {
			log.info("");
			List<ServiceProviders> serviceProviders = session
					.createCriteria(ServiceProviders.class)
					.add(Restrictions.eq("provider", provider)).list();

			if (serviceProviders != null)
				for (ServiceProviders sp : serviceProviders) {
					if (sp.getProvider().equals(provider)) {
						// Services service = sp.getService();
						// for (Services serv : services) {
						// if (!serv.equals(service))
						// services.add(service);
						services.add(sp.getService());// to be commented if
														// uncomment above!
						// }
					}
				}

			return services;
		} else
			return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohappointment.db.AppointmentDAO#getServices()
	 */
	@Override
	public Collection<Services> getServices() {
		DbSession session = sessionFactory.getCurrentSession();

		// Collection<Services> services = session.createSQLQuery(
		// "SELECT * FROM service").list();
		List<Services> services = session.createCriteria(Services.class).list();

		return services;
	}

	public Collection<Integer> getAppointmentPatientName(String nameToMatch) {

		/**
		 * 
		 nameToMatch = nameToMatch.replace(", ", " "); String[] names =
		 * nameToMatch.split(" "); List<Integer> personIds = null;
		 * 
		 * String query = "";
		 * 
		 * query =
		 * "SELECT DISTINCT(pn.person_id) FROM person_name pn INNER JOIN person p ON pn.person_id=p.person_id "
		 * + "INNER JOIN trac_vct_client t ON p.person_id=t.client_id " +
		 * "WHERE t.code_client='" + name + "'"; personIds =
		 * getSession().createSQLQuery(query).list();
		 * 
		 * if (personIds == null || personIds.size() == 0) { query =
		 * "SELECT DISTINCT(pn.person_id) FROM person_name pn INNER JOIN person p ON pn.person_id=p.person_id "
		 * + "INNER JOIN trac_vct_client t ON p.person_id=t.client_id " +
		 * "WHERE "; int i = 0; for (String n : names) { if (n != null &&
		 * n.length() > 0) { if (i > 0) query += "AND "; query +=
		 * "pn.given_name LIKE '" + n + "%' OR pn.middle_name LIKE '" + n +
		 * "%' OR pn.family_name LIKE '" + n + "%' "; i++; } }
		 * 
		 * query += "ORDER BY pn.given_name;";
		 * 
		 * personIds = getSession().createSQLQuery(query).list(); } List<Person>
		 * personList = new ArrayList<Person>(); for (Integer id : personIds)
		 * personList.add(Context.getPersonService().getPerson(id));
		 * 
		 * return personList;
		 * */

		DbSession session = sessionFactory.getCurrentSession();

		List<Integer> appointmentIds = session.createSQLQuery(
				"SELECT appointment_id FROM moh_appointment WHERE appointment_id = "
						+ nameToMatch
						+ " AND voided = FALSE AND appointment_state_id <> ")
				.list();

		return appointmentIds;
	}
}

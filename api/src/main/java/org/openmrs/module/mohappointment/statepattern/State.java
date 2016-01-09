package org.openmrs.module.mohappointment.statepattern;

import org.openmrs.module.mohappointment.model.MohAppointment;

public abstract class State {

	public void isNull() {
	}

	public MohAppointment confirmed() {
		return null;
	}

	public MohAppointment upcoming() {
		return null;
	}

	public void attended() {
	}

	public void expired() {
	}

	public void retired() {
	}

	public void postponed() {
	}

	public void inAdvance() {
	}

	public MohAppointment waiting() {
		return null;
	}

	public static State enter(MohAppointment appointment) {
		return null;
	}

	public String toString() {
		return null;
	}
}

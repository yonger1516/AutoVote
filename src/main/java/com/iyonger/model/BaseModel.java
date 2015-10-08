package com.iyonger.model;

import javax.persistence.MappedSuperclass;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Created by fuyong on 10/4/15.
 */
@MappedSuperclass
public class BaseModel<M> {
	public M merge(M source) {
		Field forDisplay = null;
		try {
			Field[] fields = getClass().getDeclaredFields();
			// Iterate over all the attributes
			for (Field each : fields) {
				if (each.isSynthetic()) {
					continue;
				}
				final int modifiers = each.getModifiers();
				if (Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers)) {
					continue;
				}
				forDisplay = each;
				if (!each.isAccessible()) {
					each.setAccessible(true);
				}
				final Object value = each.get(source);
				if (value != null) {
					each.set(this, value);
				}
			}
			return (M) this;
		} catch (Exception e) {
			String displayName = (forDisplay == null) ? "Empty" : forDisplay.getName();
			return null;
		}
	}
}

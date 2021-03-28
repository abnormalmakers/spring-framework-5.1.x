package com.qmy.imports;

import com.qmy.dao.ByImortDao;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

public class MyInportSelector implements ImportSelector {
	@Override
	public String[] selectImports(AnnotationMetadata importingClassMetadata) {
		return new String[]{ByImortDao.class.getName()};
	}
}

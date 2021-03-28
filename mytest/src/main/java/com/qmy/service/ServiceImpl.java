package com.qmy.service;

import com.qmy.dao.IndexDao;
import org.springframework.stereotype.Service;

@Service
public class ServiceImpl implements IndexService{

	private IndexDao indexDao;
//	public ServiceImpl() {
//		System.out.println("ServiceImpl");
//	}

//	public ServiceImpl(IndexDao indexDao){
//		this.indexDao = indexDao;
//	}
	@Override
	public void query() {
		System.out.println("this is ServiceImpl query");
	}
}

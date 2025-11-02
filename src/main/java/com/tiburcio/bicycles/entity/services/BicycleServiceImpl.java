package com.tiburcio.bicycles.entity.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tiburcio.bicycles.entity.dao.IBicycleDao;
import com.tiburcio.bicycles.entity.models.Bicycle;

@Service
public class BicycleServiceImpl implements IBicycleService{
	
	@Autowired
	private IBicycleDao bicycleDao;

	@Override
	public Bicycle get(long id) {
		return bicycleDao.findById(id).get();
	}

	@Override
	public List<Bicycle> getAll() {
		return (List<Bicycle>) bicycleDao.findAll();
	}

	@Override
	public void post(Bicycle bicycle) {
		bicycleDao.save(bicycle);		
	}

	@Override
	public void put(Bicycle bicycle, long id) {
		bicycleDao.findById(id).ifPresent((x) -> {
			bicycle.setId(id);
			bicycleDao.save(bicycle);
		});
	}

	@Override
	public void delete(long id) {
		bicycleDao.deleteById(id);
	}

}

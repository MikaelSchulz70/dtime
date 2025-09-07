import { BaseService } from './BaseService';
import { Headers } from './ServiceUtil';
import axios from 'axios';

const BASE_URL = "/api/specialday";

class SpecialDayService extends BaseService {
    constructor() {
        super(BASE_URL);
    }

    async getAllSpecialDays() {
        const response = await axios.get(BASE_URL);
        return response.data;
    }

    async getAvailableYears() {
        const response = await axios.get(BASE_URL + '/years');
        return response.data;
    }

    async getSpecialDaysByYear(year) {
        const response = await axios.get(`${BASE_URL}/year/${year}`);
        return response.data;
    }

    async getSpecialDay(id) {
        const response = await axios.get(`${BASE_URL}/${id}`);
        return response.data;
    }

    async createSpecialDay(specialDay) {
        const payLoad = JSON.stringify(specialDay);
        const response = await axios.post(BASE_URL, payLoad, Headers());
        return response.data;
    }

    async updateSpecialDay(id, specialDay) {
        try {
            console.log('Updating special day:', { id, specialDay });
            const specialDayWithId = { ...specialDay, id };
            const payLoad = JSON.stringify(specialDayWithId);
            const headers = Headers();
            console.log('Update headers:', headers);
            console.log('Update payload:', payLoad);

            const response = await axios.put(`${BASE_URL}/${id}`, payLoad, headers);
            console.log('Update response:', response);
            return response.data;
        } catch (error) {
            console.error('SpecialDayService.updateSpecialDay error:', error);
            console.error('Error response:', error.response);
            console.error('Error request:', error.request);
            throw error;
        }
    }

    async deleteSpecialDay(id) {
        const response = await axios.delete(`${BASE_URL}/${id}`, Headers());
        return response.data;
    }

    async deleteSpecialDaysByYear(year) {
        const response = await axios.delete(`${BASE_URL}/year/${year}`, Headers());
        return response.data;
    }

    async uploadSpecialDays(file) {
        const formData = new FormData();
        formData.append('file', file);

        const response = await axios.post(`${BASE_URL}/upload`, formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        });
        return response.data;
    }
}

export default new SpecialDayService();
package com.b2bshop.project.service;

import com.b2bshop.project.model.OrderStatus;
import com.b2bshop.project.model.User;
import com.b2bshop.project.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    public DashboardService(JwtService jwtService, UserRepository userRepository, EntityManager entityManager) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.entityManager = entityManager;
    }

    public Map<String, Object> getDashboard(HttpServletRequest request) {
        Map<String, Object> dashboardData = new HashMap<>();

        try {
            String token = request.getHeader("Authorization").split("Bearer ")[1];
            String userName = jwtService.extractUser(token);
            User user = userRepository.findByUsername(userName)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Long tenantId = user.getShop().getTenantId();
            Session session = entityManager.unwrap(Session.class);

            String orderCountQuery = "SELECT COUNT(o) " +
                    "FROM Order o " +
                    "WHERE o.shop.id = :tenantId " +
                    "AND o.orderStatus != :cancelledStatus";
            Query orderCountQueryObj = session.createQuery(orderCountQuery);
            orderCountQueryObj.setParameter("tenantId", tenantId);
            orderCountQueryObj.setParameter("cancelledStatus", OrderStatus.IPTAL_EDILDI);
            Number orderCountResult = (Number) orderCountQueryObj.uniqueResult();
            int orderCount = (orderCountResult != null) ? orderCountResult.intValue() : 0;

            String thisMonthOrderCountQuery = "SELECT COUNT(o) " +
                    " FROM Order o " +
                    " WHERE o.shop.id = :tenantId " +
                    " AND o.orderStatus != :cancelledStatus " +
                    " AND MONTH(o.orderDate) = MONTH(CURRENT_DATE())";
            Query thisMonthOrderCountQueryObj = session.createQuery(thisMonthOrderCountQuery);
            thisMonthOrderCountQueryObj.setParameter("tenantId", tenantId);
            thisMonthOrderCountQueryObj.setParameter("cancelledStatus", OrderStatus.IPTAL_EDILDI);
            Number thisMonthOrderCountResult = (Number) thisMonthOrderCountQueryObj.uniqueResult();
            int thisMonthOrderCount = (thisMonthOrderCountResult != null) ? thisMonthOrderCountResult.intValue() : 0;

            dashboardData.put("orderCount", orderCount);
            dashboardData.put("thisMonthOrderCount", thisMonthOrderCount);

            String totalRevenueQuery = "SELECT SUM(o.totalPrice) " +
                    "FROM Order o " +
                    "WHERE o.shop.id = :tenantId " +
                    "AND o.orderStatus != :cancelledStatus";
            Query totalRevenueQueryObj = session.createQuery(totalRevenueQuery);
            totalRevenueQueryObj.setParameter("tenantId", tenantId);
            totalRevenueQueryObj.setParameter("cancelledStatus", OrderStatus.IPTAL_EDILDI);
            Number totalRevenueResult = (Number) totalRevenueQueryObj.uniqueResult();
            double totalRevenue = (totalRevenueResult != null) ? totalRevenueResult.doubleValue() : 0.0;

            String thisMonthTotalRevenueQuery = "SELECT SUM(o.totalPrice) " +
                    "FROM Order o " +
                    "WHERE o.shop.id = :tenantId " +
                    "AND MONTH(o.orderDate) = MONTH(CURRENT_DATE()) " +
                    "AND o.orderStatus != :cancelledStatus";
            Query thisMonthTotalRevenueQueryObj = session.createQuery(thisMonthTotalRevenueQuery);
            thisMonthTotalRevenueQueryObj.setParameter("tenantId", tenantId);
            thisMonthTotalRevenueQueryObj.setParameter("cancelledStatus", OrderStatus.IPTAL_EDILDI);
            Number thisMonthTotalRevenueResult = (Number) thisMonthTotalRevenueQueryObj.uniqueResult();
            double thisMonthTotalRevenue = (thisMonthTotalRevenueResult != null) ? thisMonthTotalRevenueResult.doubleValue() : 0.0;

            String cancelledRevenueQuery = "SELECT SUM(o.totalPrice) " +
                    "FROM Order o " +
                    "WHERE o.shop.id = :tenantId " +
                    "AND o.orderStatus = :cancelledStatus";
            Query cancelledRevenueQueryObj = session.createQuery(cancelledRevenueQuery);
            cancelledRevenueQueryObj.setParameter("tenantId", tenantId);
            cancelledRevenueQueryObj.setParameter("cancelledStatus", OrderStatus.IPTAL_EDILDI);
            Number cancelledRevenueResult = (Number) cancelledRevenueQueryObj.uniqueResult();
            double cancelledRevenue = (cancelledRevenueResult != null) ? cancelledRevenueResult.doubleValue() : 0.0;

            String thisMonthCancelledRevenueQuery = "SELECT SUM(o.totalPrice) " +
                    "FROM Order o " +
                    "WHERE o.shop.id = :tenantId " +
                    "AND MONTH(o.orderDate) = MONTH(CURRENT_DATE()) " +
                    "AND o.orderStatus = :cancelledStatus";
            Query thisMonthCancelledRevenueQueryObj = session.createQuery(thisMonthCancelledRevenueQuery);
            thisMonthCancelledRevenueQueryObj.setParameter("tenantId", tenantId);
            thisMonthCancelledRevenueQueryObj.setParameter("cancelledStatus", OrderStatus.IPTAL_EDILDI);
            Number thisMonthCancelledRevenueResult = (Number) thisMonthCancelledRevenueQueryObj.uniqueResult();
            double thisMonthCancelledRevenue = (thisMonthCancelledRevenueResult != null) ? thisMonthCancelledRevenueResult.doubleValue() : 0.0;

            String customerCountQuery = "SELECT COUNT(c) " +
                    " FROM Customer c " +
                    " WHERE c.shop.tenantId = :tenantId";
            Query customerCountQueryObj = session.createQuery(customerCountQuery);
            customerCountQueryObj.setParameter("tenantId", tenantId);
            Number customerCountResult = (Number) customerCountQueryObj.uniqueResult();
            int customerCount = (customerCountResult != null) ? customerCountResult.intValue() : 0;

            String thisMonthCustomerCountQuery = "SELECT COUNT(c) " +
                    " FROM Customer c " +
                    " WHERE c.shop.tenantId = :tenantId " +
                    " AND MONTH(c.dateCreated) = MONTH(CURRENT_DATE())";
            Query thisMonthCustomerCountQueryObj = session.createQuery(thisMonthCustomerCountQuery);
            thisMonthCustomerCountQueryObj.setParameter("tenantId", tenantId);
            Number thisMonthCustomerCountResult = (Number) thisMonthCustomerCountQueryObj.uniqueResult();
            int thisMonthCustomerCount = (thisMonthCustomerCountResult != null) ? thisMonthCustomerCountResult.intValue() : 0;

            String productCountQuery = "SELECT COUNT(p) " +
                    " FROM Product p " +
                    " WHERE p.shop.id = :tenantId";
            Query productCountQueryObj = session.createQuery(productCountQuery);
            productCountQueryObj.setParameter("tenantId", tenantId);
            Number productCountResult = (Number) productCountQueryObj.uniqueResult();
            int productCount = (productCountResult != null) ? productCountResult.intValue() : 0;

            String thisMonthProductCountQuery = "SELECT COUNT(p) " +
                    " FROM Product p " +
                    " WHERE p.shop.id = :tenantId " +
                    " AND MONTH(p.dateCreated) = MONTH(CURRENT_DATE())";
            Query thisMonthProductCountQueryObj = session.createQuery(thisMonthProductCountQuery);
            thisMonthProductCountQueryObj.setParameter("tenantId", tenantId);
            Number thisMonthProductCountResult = (Number) thisMonthProductCountQueryObj.uniqueResult();
            int thisMonthProductCount = (thisMonthProductCountResult != null) ? thisMonthProductCountResult.intValue() : 0;

            dashboardData.put("orderCount", orderCount);
            dashboardData.put("thisMonthOrderCount", thisMonthOrderCount);
            dashboardData.put("totalRevenue", totalRevenue);
            dashboardData.put("thisMonthTotalRevenue", thisMonthTotalRevenue);
            dashboardData.put("cancelledRevenue", cancelledRevenue);
            dashboardData.put("thisMonthCancelledRevenue", thisMonthCancelledRevenue);
            dashboardData.put("customerCount", customerCount);
            dashboardData.put("thisMonthCustomerCount", thisMonthCustomerCount);
            dashboardData.put("productCount", productCount);
            dashboardData.put("thisMonthProductCount", thisMonthProductCount);

        } catch (Exception e) {
            e.printStackTrace();
            dashboardData.put("error", e.getMessage());
        }

        return dashboardData;
    }
}

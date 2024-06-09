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
            int orderCount = ((Number) orderCountQueryObj.uniqueResult()).intValue();

            String thisMonthOrderCountQuery = "SELECT COUNT(o) " +
                    " FROM Order o " +
                    " WHERE o.shop.id = :tenantId " +
                    " AND o.orderStatus != :cancelledStatus " +
                    " AND MONTH(o.orderDate) = MONTH(CURRENT_DATE())";
            Query thisMonthOrderCountQueryObj = session.createQuery(thisMonthOrderCountQuery);
            thisMonthOrderCountQueryObj.setParameter("tenantId", tenantId);
            thisMonthOrderCountQueryObj.setParameter("cancelledStatus", OrderStatus.IPTAL_EDILDI);
            int thisMonthOrderCount = ((Number) thisMonthOrderCountQueryObj.uniqueResult()).intValue();

            dashboardData.put("orderCount", orderCount);
            dashboardData.put("thisMonthOrderCount", thisMonthOrderCount);

            String cancelledOrderCountQuery = "SELECT COUNT(o) " +
                    " FROM Order o " +
                    " WHERE o.shop.id = :tenantId " +
                    " AND o.orderStatus = :cancelledStatus " +
                    " AND MONTH(o.orderDate) = MONTH(CURRENT_DATE())";
            Query cancelledOrderCountQueryObj = session.createQuery(cancelledOrderCountQuery);
            cancelledOrderCountQueryObj.setParameter("tenantId", tenantId);
            cancelledOrderCountQueryObj.setParameter("cancelledStatus", OrderStatus.IPTAL_EDILDI);
            int thisMonthCancelledOrderCount = ((Number) cancelledOrderCountQueryObj.uniqueResult()).intValue();

            dashboardData.put("thisMonthCancelledOrderCount", thisMonthCancelledOrderCount);

            String totalCancelledOrderCountQuery = "SELECT COUNT(o) " +
                    " FROM Order o " +
                    " WHERE o.shop.id = :tenantId " +
                    " AND o.orderStatus = :cancelledStatus";
            Query totalCancelledOrderCountQueryObj = session.createQuery(totalCancelledOrderCountQuery);
            totalCancelledOrderCountQueryObj.setParameter("tenantId", tenantId);
            totalCancelledOrderCountQueryObj.setParameter("cancelledStatus", OrderStatus.IPTAL_EDILDI);
            int cancelledOrderCount = ((Number) totalCancelledOrderCountQueryObj.uniqueResult()).intValue();

            dashboardData.put("cancelledOrderCount", cancelledOrderCount);

            String totalRevenueQuery = "SELECT SUM(o.totalPrice) " +
                    " FROM Order o " +
                    " WHERE o.shop.id = :tenantId";
            Query totalRevenueQueryObj = session.createQuery(totalRevenueQuery);
            totalRevenueQueryObj.setParameter("tenantId", tenantId);
            double totalRevenue = ((Number) totalRevenueQueryObj.getSingleResult()).doubleValue();

            String thisMonthTotalRevenueQuery = "SELECT SUM(o.totalPrice) " +
                    " FROM Order o " +
                    " WHERE o.shop.id = :tenantId " +
                    " AND MONTH(o.orderDate) = MONTH(CURRENT_DATE())";
            Query thisMonthTotalRevenueQueryObj = session.createQuery(thisMonthTotalRevenueQuery);
            thisMonthTotalRevenueQueryObj.setParameter("tenantId", tenantId);
            double thisMonthTotalRevenue = ((Number) thisMonthTotalRevenueQueryObj.getSingleResult()).doubleValue();

            String customerCountQuery = "SELECT COUNT(c) " +
                    " FROM Customer c " +
                    " WHERE c.shop.tenantId = :tenantId";
            Query customerCountQueryObj = session.createQuery(customerCountQuery);
            customerCountQueryObj.setParameter("tenantId", tenantId);
            int customerCount = ((Number) customerCountQueryObj.getSingleResult()).intValue();

            String thisMonthCustomerCountQuery = "SELECT COUNT(c) " +
                    " FROM Customer c " +
                    " WHERE c.shop.tenantId = :tenantId " +
                    " AND MONTH(c.dateCreated) = MONTH(CURRENT_DATE())";
            Query thisMonthCustomerCountQueryObj = session.createQuery(thisMonthCustomerCountQuery);
            thisMonthCustomerCountQueryObj.setParameter("tenantId", tenantId);
            int thisMonthCustomerCount = ((Number) thisMonthCustomerCountQueryObj.getSingleResult()).intValue();

            String productCountQuery = "SELECT COUNT(p) " +
                    " FROM Product p " +
                    " WHERE p.shop.id = :tenantId";
            Query productCountQueryObj = session.createQuery(productCountQuery);
            productCountQueryObj.setParameter("tenantId", tenantId);
            int productCount = ((Number) productCountQueryObj.getSingleResult()).intValue();

            String thisMonthProductCountQuery = "SELECT COUNT(p) " +
                    " FROM Product p " +
                    " WHERE p.shop.id = :tenantId " +
                    " AND MONTH(p.dateCreated) = MONTH(CURRENT_DATE())";
            Query thisMonthProductCountQueryObj = session.createQuery(thisMonthProductCountQuery);
            thisMonthProductCountQueryObj.setParameter("tenantId", tenantId);
            int thisMonthProductCount = ((Number) thisMonthProductCountQueryObj.getSingleResult()).intValue();

            dashboardData.put("orderCount", orderCount);
            dashboardData.put("thisMonthOrderCount", thisMonthOrderCount);
            dashboardData.put("totalRevenue", totalRevenue);
            dashboardData.put("thisMonthTotalRevenue", thisMonthTotalRevenue);
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

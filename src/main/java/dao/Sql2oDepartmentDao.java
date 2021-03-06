package dao;

import models.Department;
import models.News;
import models.User;
import org.sql2o.Connection;
import org.sql2o.Sql2oException;

import java.util.ArrayList;
import java.util.List;

public class Sql2oDepartmentDao implements DepartmentDao {
    public Sql2oDepartmentDao(){}

    @Override
    public void add(Department department) {
        String sql = "INSERT INTO departments (name,description,totalEmployees) VALUES (:name,:description,:totalEmployees)";
        try (Connection con = DB.sql2o.open()) {
            int id = (int) con.createQuery(sql,true)
                    .bind(department)
                    .addParameter("totalEmployees",department.getTotalEmployees())
                    .executeUpdate()
                    .getKey();
            department.setId(id);
        } catch (Sql2oException ex){
            System.out.println(ex);
        }
    }

    @Override
    public void addUserToDepartment(Department department,User user) {
        String sql = "INSERT INTO departments_users(departmentId,userId) values (:departmentId,:userId);";
        try (Connection con = DB.sql2o.open()) {
            con.createQuery(sql)
                    .addParameter("departmentId",department.getId())
                    .addParameter("userId",user.getId())
                    .executeUpdate();
            user.setDepartment(department.getName());
            department.incrementTotalEmployees();
            updateEmployeeCount(department);
        } catch (Sql2oException ex){
            System.out.println("Cannot insert user into department: " + ex);
        }
    }


    @Override
    public Department findById(int id) {
        String sql = "SELECT * from departments WHERE id=:id;";
        try (Connection con = DB.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetchFirst(Department.class);
        }
    }

    @Override
    public List<Department> allDepartments() {
        String sql = "SELECT * from departments;";
        try (Connection con = DB.sql2o.open()) {
            return con.createQuery(sql)
                    .executeAndFetch(Department.class);
        }
    }

    @Override
    public List<User> allDepartmentEmployees(int departmentId) {
        List<User> employees = new ArrayList<>();

        String joinQuery = "SELECT userId FROM departments_users WHERE departmentId = :departmentId";
        try (Connection con = DB.sql2o.open()) {
            List<Integer> userIds = con.createQuery(joinQuery)
                    .addParameter("departmentId",departmentId)
                    .executeAndFetch(Integer.class);

            for(int uId:userIds){
                String sql = "SELECT * FROM users WHERE id = :id";
                employees.add(con.createQuery(sql)
                        .addParameter("id",uId)
                        .executeAndFetchFirst(User.class));
            }

        } catch (Sql2oException ex){
            System.out.println("Cannot retrieve requested employees: " +ex);
        }

        return employees;
    }

    @Override
    public List<News> allDepartmentNews(int departmentId) {
        List<News> news = new ArrayList<>();

        String joinQuery = "SELECT newsId FROM departments_news WHERE departmentId = :departmentId";
        try (Connection con = DB.sql2o.open()) {
            List<Integer> newsIDs = con.createQuery(joinQuery)
                    .addParameter("departmentId",departmentId)
                    .executeAndFetch(Integer.class);

            for(int nId:newsIDs){
                String sql = "SELECT * FROM news WHERE id = :id";
                news.add(con.createQuery(sql)
                        .addParameter("id",nId)
                        .executeAndFetchFirst(News.class));
            }

        } catch (Sql2oException ex){
            System.out.println("Cannot retrieve selected news: " +ex);
        }
        return news;
    }

    @Override
    public void deleteDepartmentById(int id) {
        String sql = "DELETE from departments WHERE id = :id";
        try (Connection con = DB.sql2o.open()) {
            con.createQuery(sql)
                    .addParameter("id", id)
                    .executeUpdate();
        } catch (Sql2oException ex){
            System.out.println(ex);
        }
    }

    @Override
    public void deleteEmployeeFromDepartment(Department department,User user) {
        String sql = "DELETE from departments_users WHERE departmentId = :departmentId AND userId = :userId";
        try (Connection con = DB.sql2o.open()) {
            con.createQuery(sql)
                    .addParameter("departmentId", department.getId())
                    .addParameter("userId", user.getId())
                    .executeUpdate();
            user.setDepartment("None");
            department.decrementTotalEmployees();
            updateEmployeeCount(department);
        } catch (Sql2oException ex){
            System.out.println(ex);
        }
    }

    @Override
    public void deleteDepartmentNewsById(int departmentId, int newsId) {
        String sql = "DELETE from departments_news WHERE departmentId = :departmentId AND newsId = :newsId";
        try (Connection con = DB.sql2o.open()) {
            con.createQuery(sql)
                    .addParameter("departmentId", departmentId)
                    .addParameter("newsId", newsId)
                    .executeUpdate();
        } catch (Sql2oException ex){
            System.out.println(ex);
        }
    }

    @Override
    public void deleteAll() {
        String sql = "DELETE from departments";
        try (Connection con = DB.sql2o.open()) {
            con.createQuery(sql).executeUpdate();
        } catch (Sql2oException ex) {
            System.out.println(ex);
        }
    }

    @Override
    public void updateEmployeeCount(Department department) {
        String sql = "UPDATE departments SET totalEmployees = :totalEmployees WHERE id = :id";
        try (Connection con = DB.sql2o.open()) {
            con.createQuery(sql)
                    .addParameter("totalEmployees",department.getTotalEmployees())
                    .addParameter("id",department.getId())
                    .executeUpdate();
        } catch (Sql2oException ex) {
            System.out.println(ex);
        }
    }
}
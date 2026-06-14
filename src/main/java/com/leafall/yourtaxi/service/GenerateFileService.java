package com.leafall.yourtaxi.service;

import com.leafall.yourtaxi.dto.generatedFiles.DownloadFileResponseDto;
import com.leafall.yourtaxi.dto.generatedFiles.GeneratedFileResponseDto;
import com.leafall.yourtaxi.entity.GeneratedFileEntity;
import com.leafall.yourtaxi.entity.OrderEntity;
import com.leafall.yourtaxi.entity.PointEntity;
import com.leafall.yourtaxi.entity.enums.GeneratedFileStatus;
import com.leafall.yourtaxi.entity.enums.OrderPaymentType;
import com.leafall.yourtaxi.entity.enums.OrderStatus;
import com.leafall.yourtaxi.exception.BadRequestException;
import com.leafall.yourtaxi.exception.NotFoundException;
import com.leafall.yourtaxi.mapper.GeneratedFileMapper;
import com.leafall.yourtaxi.repository.GeneratedFileRepository;
import com.leafall.yourtaxi.repository.OrderRepository;
import com.leafall.yourtaxi.repository.UserRepository;
import com.leafall.yourtaxi.utils.TimeUtils;
import com.leafall.yourtaxi.utils.pagination.PaginationCursor;
import com.leafall.yourtaxi.utils.pagination.PaginationParams;
import com.leafall.yourtaxi.utils.pagination.PaginationResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenerateFileService {

    private final static String TEMPLATE_PATH_EXCEL = "templates/templateReport.xlsm";
    private final GeneratedFileRepository generatedFileRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final GeneratedFileMapper mapper;

    @Transactional(readOnly = true)
    public PaginationResponse<GeneratedFileResponseDto> findAll(PaginationParams params) {
        var pagination = params.getPageable(false, "createdAt");
        var files = generatedFileRepository.findAll(pagination);
        var content = files.stream().map(mapper::mapToDto).toList();
        return new PaginationResponse<>(content, new PaginationCursor(params, files.getTotalElements()));
    }

    @Transactional(readOnly = true)
    public GeneratedFileResponseDto findById(UUID id) {
        var file = generatedFileRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("base.error.not-found"));
        return mapper.mapToDto(file);
    }

    @Transactional(readOnly = true)
    @SneakyThrows
    public DownloadFileResponseDto download(UUID id) {

        var file = generatedFileRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("base.error.not-found"));
        var dto = new DownloadFileResponseDto();
        dto.setFilename(file.getPath());
        if (!file.getStatus().equals(GeneratedFileStatus.COMPLETED)) {
            return dto;
        }
        Path path = Paths.get("uploads/reports", file.getPath());
        Resource resource = new UrlResource(path.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new NotFoundException("base.error.not-found");
        }
        dto.setResource(resource);
        return dto;
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneratedFileResponseDto generateExcel(UUID userId, java.sql.Date fromDate, java.sql.Date toDate) {
        var orders = orderRepository.findAllByCreatedAtBetween(fromDate.getTime(), toDate.getTime());
        var generatedFile = createDefault(userId);
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        startGenerateExcel(orders, fromDate, generatedFile.getId());
                    }
                }
        );
        return generatedFile;
    }

    @Async
    @SneakyThrows
    public void startGenerateExcel(List<OrderEntity> orders, Date date, UUID id) {
        var generatedFile = generatedFileRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("base.error.not-found"));
        generatedFile.setStatus(GeneratedFileStatus.IN_PROCESSED);
        var toUpdated = generatedFileRepository.save(generatedFile);
        try (var templateStream = getClass().getClassLoader().getResourceAsStream(TEMPLATE_PATH_EXCEL)) {
            if (templateStream == null) throw new FileNotFoundException("Template not found");
            try (final var workbook = new XSSFWorkbook(templateStream)) {
                var formatterMonthAndYear = new SimpleDateFormat("MMMM.yyyy");
                final var sheet  = workbook.getSheet("ORDERS");
                setCellValue(sheet, 1, 11, formatterMonthAndYear.format(date).split("\\.")[0]);
                setCellValue(sheet, 1, 12, formatterMonthAndYear.format(date).split("\\.")[1]);

                int startRow = 7;

                var formatter = new SimpleDateFormat("dd.MM.yyyy");
                var formatterTime = new SimpleDateFormat("hh:mm:ss");
                for (int i = 0; i < orders.size(); i++) {
                    OrderEntity order = orders.get(i);
                    int currentRowIdx = startRow + i;
                    XSSFRow row = sheet.getRow(currentRowIdx);
                    if (row == null) row = sheet.createRow(currentRowIdx);
                    setCellValue(row, 0, i + 1);
                    setCellValue(row, 1, "ИП Курошко А.С.");
                    setCellValue(row, 2, "491682282");
                    setCellValue(row, 3, "80296496478");
                    setCellValue(row, 4, "ИП Курошко А.С.");
                    setCellValue(row, 5, "491682282");
                    var executor = order.getExecutor();
                    if (executor != null) {
                        setCellValue(row, 6, executor.getCar().getMark());
                        setCellValue(row, 7, executor.getCar().getNumber());
                        setCellValue(row, 8, executor.getUser().getFullName());
                        setCellValue(row, 9, formatter.format(new Date(executor.getCreatedAt())));
                        if (executor.getEndAt() != null) {
                            setCellValue(row, 10, formatter.format(new Date(executor.getEndAt())));
                        }
                    }
                    var createdAt = order.getCreatedAt();
                    setCellValue(row, 11, formatter.format(new Date(createdAt)));
                    setCellValue(row, 12, formatterTime.format(new Date(createdAt)));
                    order.getPoints().sort(Comparator.comparingInt(PointEntity::getIndex));
                    var point1 = order.getPoints().get(0);
                    var point2 = order.getPoints().get(order.getPoints().size() - 1);
                    setCellValue(row, 18, point1.getName());
                    setCellValue(row, 19, point1.getName());
                    setCellValue(row, 20, point2.getName());
                    setCellValue(row, 21, String.format("AT - %.2f", order.getPrice()));
                    setCellValue(row, 22, order.getPaymentType() == OrderPaymentType.CASH ? "наличные" : "безналичные");
                    setCellValue(row, 23, order.getStatus() == OrderStatus.COMPLETED ? "выполнен" : "не выполнен");
                }
                var out = new ByteArrayOutputStream();
                workbook.write(out);
                byte[] reportBytes = out.toByteArray();
                String fileName = "report_" + TimeUtils.getCurrentTimeFromUTC() + ".xlsm";
                Path filePath = Paths.get("uploads/reports/", fileName);
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, reportBytes);
                toUpdated.setPath(fileName);
            }
        }
        toUpdated.setStatus(GeneratedFileStatus.COMPLETED);
        var savedTwo = generatedFileRepository.save(toUpdated);
    }

    private GeneratedFileResponseDto createDefault(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user.error.not-found"));
        var entity = new GeneratedFileEntity();
        entity.setUser(user);
        entity.setPath("");
        entity.setStatus(GeneratedFileStatus.NEW);
        var toSaved = generatedFileRepository.save(entity);
        return mapper.mapToDto(toSaved);
    }

    private void setCellValue(XSSFSheet sheet, int rowIdx, int colIdx, String value) {
        XSSFRow row = sheet.getRow(rowIdx);
        if (row == null) row = sheet.createRow(rowIdx);
        XSSFCell cell = row.getCell(colIdx);
        if (cell == null) cell = row.createCell(colIdx);
        cell.setCellValue(value);
    }

    private void setCellValue(XSSFRow row, int colIdx, Object value) {
        XSSFCell cell = row.getCell(colIdx);
        if (cell == null) cell = row.createCell(colIdx);

        if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else {
            cell.setCellValue(value.toString());
        }
    }
}

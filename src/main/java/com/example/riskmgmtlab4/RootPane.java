package com.example.riskmgmtlab4;

import com.dlsc.formsfx.view.util.ViewMixin;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.StringConverter;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class RootPane extends VBox implements ViewMixin {
    public static final Font INPUT_FONT = Font.font("Arial", 25);
    public static final String INT_REGEX = "[0-9]*";
    public static final String DOUBLE_REGEX = "[0-9]{1,13}(\\.[0-9]*)?";
    private static final int TECHNICAL = 0;
    private static final int COST = 1;
    private static final int PLANNING = 2;
    private static final int MANAGING = 3;


    private ScrollPane scrollContent;
    // First section
    private NamedTable<RiskSourceRow> technicalRiskSourcesTable;
    private NamedTable<RiskSourceRow> costRiskSourcesTable;
    private NamedTable<RiskSourceRow> planningRiskSourcesTable;
    private NamedTable<RiskSourceRow> managingRiskSourcesTable;

    // Second section
    private TextField numExperts;
    private Button calculateButton;

    // Third section
    private Label probabilityOfRiskOccurrenceSectionHeader;
    private NamedTable<RiskProbabilityExpertEvaluationRow> riskProbabilityExpertEvaluationTable;
    private NamedTable<ExpertEvaluationIncludingCostRow> expertEvaluationIncludingCostTable;
    private NamedTable<CoefficientsOfRiskSignificanceRow> coeffsOfSignificanceTable;
    private NamedTable<RiskProbabilityRow> riskProbabilityTable;

    // Fourth section
    private Label lossInCaseOfRiskOccurrenceSectionHeader;
    private NamedTable<LossesRiskRow> lossesRisksTable;
    private NamedTable<LossExpertEvaluationIncludingCostRow> lossExpertEvaluationIncludingCostTable;
    private NamedTable<ProjectCostDistribution> projectCostDistributionTable;

    public RootPane() {
        init();
    }

    private Label createSectionLabel(String text) {
        Label label = new Label(text);
        label.setFont(new Font(INPUT_FONT.getName(), 35));
        label.setPadding(new Insets(40));
        return label;
    }

    private <T> TableView<T> initTable(List<TableColumn<T, ?>> columns) {
        TableView<T> tableView = new TableView<>();
        tableView.setVisible(true);
        tableView.setEditable(true);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tableView.getColumns().addAll(columns);
        return tableView;
    }

    private <S, T> TableColumn<S, T> createColumn(String headerName, Function<S, ObservableValue<T>> function) {
        TableColumn<S, T> tableColumn = new TableColumn<>(headerName);
        tableColumn.setCellValueFactory(param -> function.apply(param.getValue()));
        return tableColumn;
    }

    private <S, T, K extends WritableValue<T> & Property<T>> TableColumn<S, T> createEditableColumn(
            String headerName,
            Function<S, K> function,
            Supplier<TableView<S>> tableView,
            StringConverter<T> stringConverter
    ) {
        TableColumn<S, T> tableColumn = new TableColumn<>(headerName);
        tableColumn.setEditable(true);
        tableColumn.setCellFactory(TextFieldTableCell.forTableColumn(stringConverter));
        tableColumn.setCellValueFactory(param -> function.apply(param.getValue()));
        tableColumn.setOnEditCommit(event -> {
            S objToUpdate = tableView.get().getItems().get(event.getTablePosition().getRow());
            function.apply(objToUpdate).setValue(event.getNewValue());
        });
        return tableColumn;
    }

    private <S, T extends Number, K extends WritableValue<T> & Property<T>> TableColumn<S, T> createEditableDoubleColumn(
            String headerName,
            Function<S, K> function,
            Supplier<TableView<S>> tableView,
            StringConverter<T> stringConverter
    ) {
        TableColumn<S, T> tableColumn = new TableColumn<>(headerName);
        tableColumn.setEditable(true);
        tableColumn.setCellFactory(TextFieldTableCell.forTableColumn(stringConverter));
        tableColumn.setCellValueFactory(param -> function.apply(param.getValue()));
        tableColumn.setOnEditCommit(event -> {
            T newValue = event.getNewValue();
            if (newValue == null) {
                return;
            }
            S objToUpdate = tableView.get().getItems().get(event.getTablePosition().getRow());
            function.apply(objToUpdate).setValue(newValue);
        });
        return tableColumn;
    }

    private <S> TableColumn<S, CheckBox> createFlagColumn(String headerName, Function<S, Property<Boolean>> function) {
        TableColumn<S, CheckBox> tableColumn = new TableColumn<>(headerName);
        tableColumn.setCellValueFactory(arg -> {
            S obj = arg.getValue();
            CheckBox checkBox = new CheckBox();
            checkBox.selectedProperty().bindBidirectional(function.apply(obj));
            return new SimpleObjectProperty<>(checkBox);
        });
        return tableColumn;
    }

    @Override
    public void initializeParts() {
        scrollContent = new ScrollPane();

        probabilityOfRiskOccurrenceSectionHeader = createSectionLabel("Визначення ймовірності настання ризикових подій");
        probabilityOfRiskOccurrenceSectionHeader.setVisible(false);

        lossInCaseOfRiskOccurrenceSectionHeader = createSectionLabel("Визначення величини збитків від прояву ризику (математичне сподівання збитку)");
        lossInCaseOfRiskOccurrenceSectionHeader.setVisible(false);

        technicalRiskSourcesTable = new NamedTable<>(initTable(createRiskSourcesColumns(() -> technicalRiskSourcesTable.getTableView())), "Множина джерел появи технічних ризиків");
        costRiskSourcesTable = new NamedTable<>(initTable(createRiskSourcesColumns(() -> costRiskSourcesTable.getTableView())), "Множина джерел появи вартісних ризиків");
        planningRiskSourcesTable = new NamedTable<>(initTable(createRiskSourcesColumns(() -> planningRiskSourcesTable.getTableView())), "Множина джерел появи планових ризиків");
        managingRiskSourcesTable = new NamedTable<>(initTable(createRiskSourcesColumns(() -> managingRiskSourcesTable.getTableView())), "Множина джерел появи ризиків реалізації процесів і процедур управління програмним проектом");

        projectCostDistributionTable = new NamedTable<>(initTable(getProjectCostDistributionColumns()), "Розподіл вартості реалізації проекту за множинами ризиків");
        projectCostDistributionTable.disable();

        calculateButton = new Button("Обрахувати");
        calculateButton.setFont(INPUT_FONT);
        numExperts = new TextField("10");
        numExperts.setFont(INPUT_FONT);
        numExperts.setTextFormatter(new TextFormatter<>(change -> {
            var text = change.getText();
            return text.matches(INT_REGEX) ? change : null;
        }));

        riskProbabilityExpertEvaluationTable = new NamedTable<>(initTable(List.of()), "Ймовірності настання ризикових подій, встановлені експертами");
        riskProbabilityExpertEvaluationTable.disable();

        expertEvaluationIncludingCostTable = new NamedTable<>(initTable(List.of()), "Оцінки експертів з урахуванням їхньої вагомості");
        expertEvaluationIncludingCostTable.disable();

        riskProbabilityTable = new NamedTable<>(initTable(createRiskProbabilityTableColumns()), "Ймовірності настання ризикових подій");
        riskProbabilityTable.disable();

        lossesRisksTable = new NamedTable<>(initTable(
                createLossesRiskTableColumns(() -> lossesRisksTable.getTableView())),
                "Розмір можливих збитків від настання ризику");
        lossesRisksTable.disable();

        lossExpertEvaluationIncludingCostTable = new NamedTable<>(initTable(List.of()),
                "Оцінки експертів з урахуванням їхньої вагомості");
        lossExpertEvaluationIncludingCostTable.disable();

        coeffsOfSignificanceTable = new NamedTable<>(
                initTable(List.of()),
                "Коефіцієнти вагомості кожного за множинами ризиків"
        );
        coeffsOfSignificanceTable.disable();
    }

    private List<TableColumn<ProjectCostDistribution, ?>> getProjectCostDistributionColumns() {
        return List.of(
                createColumn("Стадія", ProjectCostDistribution::name),
                createColumn("1", ProjectCostDistribution::technical),
                createColumn("2", ProjectCostDistribution::cost),
                createColumn("3", ProjectCostDistribution::planning),
                createColumn("4", ProjectCostDistribution::managing),
                createColumn("Сума", ProjectCostDistribution::sum)
        );
    }

    private List<TableColumn<RiskProbabilityRow, ?>> createRiskProbabilityTableColumns() {
        return List.of(
                createColumn("Тип", RiskProbabilityRow::riskType),
                createColumn("Ймовірність", RiskProbabilityRow::probability),
                createColumn("Класифікація настання", v -> new SimpleStringProperty(classifyRisk(v.probability().getValue().doubleValue())))
        );
    }

    @Override
    public void setupEventHandlers() {
        calculateButton.setOnAction(event -> performCalculations());
    }

    @Override
    public void layoutParts() {
        var vBox = new VBox();
        vBox.getChildren().add(technicalRiskSourcesTable.asNode());
        vBox.getChildren().add(costRiskSourcesTable.asNode());
        vBox.getChildren().add(planningRiskSourcesTable.asNode());
        vBox.getChildren().add(managingRiskSourcesTable.asNode());
        vBox.getChildren().add(centered(namedInput(numExperts, "Кількість експертів")));
        vBox.getChildren().add(centered(calculateButton));
        vBox.getChildren().add(centered(probabilityOfRiskOccurrenceSectionHeader));
        vBox.getChildren().add(coeffsOfSignificanceTable.asNode());
        vBox.getChildren().add(riskProbabilityExpertEvaluationTable.asNode());
        vBox.getChildren().add(expertEvaluationIncludingCostTable.asNode());
        vBox.getChildren().add(riskProbabilityTable.asNode());
        vBox.getChildren().add(centered(lossInCaseOfRiskOccurrenceSectionHeader));
        vBox.getChildren().add(lossesRisksTable.asNode());
        vBox.getChildren().add(projectCostDistributionTable.asNode());
        vBox.getChildren().add(lossExpertEvaluationIncludingCostTable.asNode());
        scrollContent.setContent(vBox);
        scrollContent.setFitToWidth(true);
        getChildren().add(scrollContent);
        fillTables();
    }

    private void performCalculations() {
        probabilityOfRiskOccurrenceSectionHeader.setVisible(true);
        lossInCaseOfRiskOccurrenceSectionHeader.setVisible(true);

        var numExperts = getNumberOfExperts();

        setupCoeffisOfSignificanceTable(numExperts);
        setupRiskProbabilityExpertEvaluationTable(numExperts);

        enrichRiskProbabilityExpertTable(numExperts, technicalRiskSourcesTable.getItems());
        enrichRiskProbabilityExpertTable(numExperts, costRiskSourcesTable.getItems());
        enrichRiskProbabilityExpertTable(numExperts, planningRiskSourcesTable.getItems());
        enrichRiskProbabilityExpertTable(numExperts, managingRiskSourcesTable.getItems());

        setupExpertEvaluationIncludingCostTable(numExperts);

        Property<Double> technicalRisk = enrichExpertEvaluationIncludingCostTable(TECHNICAL, numExperts, technicalRiskSourcesTable.getItems());
        Property<Double> costRisk = enrichExpertEvaluationIncludingCostTable(COST, numExperts, costRiskSourcesTable.getItems());
        Property<Double> planningRisk = enrichExpertEvaluationIncludingCostTable(PLANNING, numExperts, planningRiskSourcesTable.getItems());
        Property<Double> managingRisk = enrichExpertEvaluationIncludingCostTable(MANAGING, numExperts, managingRiskSourcesTable.getItems());

        createRiskProbabilityTable(technicalRisk, costRisk, planningRisk, managingRisk);

        setupLossesRisksTable();

        var technicalInitSum = enrichLossesRisksTable(numExperts, technicalRiskSourcesTable.getItems());
        var costInitSum = enrichLossesRisksTable(numExperts, costRiskSourcesTable.getItems());
        var planningInitSum = enrichLossesRisksTable(numExperts, planningRiskSourcesTable.getItems());
        var managingInitSum = enrichLossesRisksTable(numExperts, managingRiskSourcesTable.getItems());

        setupLossExpertEvaluationIncludingCostTable();

        var technicalFinalSum = enrichLossExpertEvaluationIncludingCostTable(technicalRiskSourcesTable.getItems(), TECHNICAL, numExperts);
        var costFinalSum = enrichLossExpertEvaluationIncludingCostTable(costRiskSourcesTable.getItems(), COST, numExperts);
        var planningFinalSum = enrichLossExpertEvaluationIncludingCostTable(planningRiskSourcesTable.getItems(), PLANNING, numExperts);
        var managingFinalSum = enrichLossExpertEvaluationIncludingCostTable(managingRiskSourcesTable.getItems(), MANAGING, numExperts);

        projectCostDistributionTable.clearItems();
        projectCostDistributionTable.getItems().addAll(
                new ProjectCostDistribution(
                        new SimpleStringProperty("Початкова"),
                        technicalInitSum,
                        costInitSum,
                        planningInitSum,
                        managingInitSum,
                        dynamic(
                                dynamic(costInitSum, technicalInitSum, Double::sum),
                                dynamic(planningInitSum, managingInitSum, Double::sum),
                                Double::sum
                        )
                ),
                new ProjectCostDistribution(
                        new SimpleStringProperty("Кінцева"),
                        technicalFinalSum,
                        costFinalSum,
                        planningFinalSum,
                        managingFinalSum,
                        dynamic(
                                dynamic(costFinalSum, technicalFinalSum, Double::sum),
                                dynamic(planningFinalSum, managingFinalSum, Double::sum),
                                Double::sum
                        )
                )
        );
        projectCostDistributionTable.enable();
        calculatePriorities();
    }

    private Property<Double> enrichLossExpertEvaluationIncludingCostTable(
            ObservableList<RiskSourceRow> riskSources,
            int rowIndex,
            int numExperts
    ) {
        List<Property<Double>> vars = new ArrayList<>();
        for (RiskSourceRow riskSourceRow : riskSources) {
            if (!Boolean.TRUE.equals(riskSourceRow.isEnabled().getValue())) {
                continue;
            }
            var riskName = riskSourceRow.notation().get();
            var expertEvalRow = lossesRisksTable.getItems()
                    .stream()
                    .filter(x -> x.riskNotation().get().equals(riskName))
                    .findFirst()
                    .orElseThrow();

            var expertEvaluation = IntStream.range(0, numExperts)
                    .boxed()
                    .map(v -> {
                        var coeffOfSignificance = coeffsOfSignificanceTable.getItems().get(rowIndex).expertCoefficients().get(v);
                        var expertP = expertEvalRow.proportionOfPossibleLoss().get(v);
                        return dynamic(coeffOfSignificance, expertP, (a, b) -> a.doubleValue() * b);
                    })
                    .toList();

            Property<Double> sumEval = dynamic(expertEvaluation, l -> l.stream().mapToDouble(ObservableValue::getValue).sum());
            Property<Double> additionalCost = dynamic(
                    dynamic(sumEval, expertEvalRow.averageExpectedCost(), (a, b) -> a / b),
                    expertEvalRow.riskInitialCost(),
                    (a, b) -> a * b
            );
            additionalCost.addListener((observable, oldValue, newValue) -> calculatePriorities());
            Property<Double> fullCost = dynamic(additionalCost, expertEvalRow.riskInitialCost(), Double::sum);

            vars.add(fullCost);

            var lossExpertEvaluationIncludingCostRow = new LossExpertEvaluationIncludingCostRow(
                    new SimpleStringProperty(riskName),
                    expertEvaluation,
                    additionalCost,
                    fullCost,
                    new SimpleStringProperty("XX")
            );
            lossExpertEvaluationIncludingCostTable.getItems().add(lossExpertEvaluationIncludingCostRow);
        }
        return dynamic(vars, l -> l.stream().mapToDouble(ObservableValue::getValue).sum());
    }

    private Node centered(Node node) {
        BorderPane root = new BorderPane();
        BorderPane.setAlignment(node, Pos.CENTER);
        root.setCenter(node);
        return root;
    }

    private void setupLossExpertEvaluationIncludingCostTable() {
        lossExpertEvaluationIncludingCostTable.getColumns().clear();
        lossExpertEvaluationIncludingCostTable
                .getColumns()
                .addAll(createLossExpertEvaluationIncludingCostColumns(() -> lossExpertEvaluationIncludingCostTable.getTableView()));
        lossExpertEvaluationIncludingCostTable.clearItems();
        lossExpertEvaluationIncludingCostTable.enable();
    }

    private List<TableColumn<LossExpertEvaluationIncludingCostRow, ?>> createLossExpertEvaluationIncludingCostColumns(
            Supplier<TableView<LossExpertEvaluationIncludingCostRow>> tableView
    ) {
        var numOfExperts = getNumberOfExperts();
        List<TableColumn<LossExpertEvaluationIncludingCostRow, ?>> columns = new ArrayList<>();
        columns.add(createColumn("Ризик", LossExpertEvaluationIncludingCostRow::riskNotation));
        for (int i = 0; i < numOfExperts; i++) {
            int expertIndex = i;
            columns.add(createColumn(
                    "Оцінка від експерту " + (i + 1),
                    v -> v.expertEvaluation().get(expertIndex)));
        }
        columns.add(createColumn("Додаткова вартість", LossExpertEvaluationIncludingCostRow::additionalCost));
        columns.add(createColumn("Кінцева вартість", LossExpertEvaluationIncludingCostRow::finalCost));
        columns.add(createColumn("Пріоритет", LossExpertEvaluationIncludingCostRow::priority));
        return columns;
    }

    private void setupLossesRisksTable() {
        lossesRisksTable.getColumns().clear();
        lossesRisksTable
                .getColumns()
                .addAll(createLossesRiskTableColumns(() -> lossesRisksTable.getTableView()));
        lossesRisksTable.clearItems();
        lossesRisksTable.enable();
    }

    private void createRiskProbabilityTable(Property<Double> technicalRisk, Property<Double> costRisk, Property<Double> planningRisk, Property<Double> managingRisk) {
        riskProbabilityTable.clearItems();
        riskProbabilityTable.enable();
        riskProbabilityTable.getItems().addAll(
                new RiskProbabilityRow(
                        new SimpleStringProperty("Технічних"),
                        technicalRisk
                ),
                new RiskProbabilityRow(
                        new SimpleStringProperty("Вартісних"),
                        costRisk
                ),
                new RiskProbabilityRow(
                        new SimpleStringProperty("Планування"),
                        planningRisk
                ),
                new RiskProbabilityRow(
                        new SimpleStringProperty("Реалізації процесів і процедур управління програмним проектом"),
                        managingRisk
                ));
    }

    private void setupExpertEvaluationIncludingCostTable(int numExperts) {
        expertEvaluationIncludingCostTable.enable();
        expertEvaluationIncludingCostTable.getColumns().clear();
        expertEvaluationIncludingCostTable
                .getColumns()
                .addAll(createExpertEvaluationIncludingCostTable(numExperts, () -> expertEvaluationIncludingCostTable.getTableView()));
        expertEvaluationIncludingCostTable.clearItems();
    }

    private void setupRiskProbabilityExpertEvaluationTable(int numExperts) {
        riskProbabilityExpertEvaluationTable.enable();
        riskProbabilityExpertEvaluationTable.getColumns().clear();
        riskProbabilityExpertEvaluationTable
                .getColumns()
                .addAll(createRiskProbabilityExpertEvaluationTableColumns(numExperts, () -> riskProbabilityExpertEvaluationTable.getTableView()));
        riskProbabilityExpertEvaluationTable.clearItems();
    }

    private void setupCoeffisOfSignificanceTable(int numExperts) {
        coeffsOfSignificanceTable.getColumns().clear();
        coeffsOfSignificanceTable
                .getColumns()
                .addAll(createCoeffsOfSignificanceTableColumns(numExperts, () -> coeffsOfSignificanceTable.getTableView()));
        coeffsOfSignificanceTable.clearItems();
        coeffsOfSignificanceTable.getItems().addAll(
                new CoefficientsOfRiskSignificanceRow(
                        new SimpleStringProperty("Множина настання технічних ризикових подій"),
                        createIntProperties(numExperts)
                ),
                new CoefficientsOfRiskSignificanceRow(
                        new SimpleStringProperty("Множина настання вартісних ризикових подій"),
                        createIntProperties(numExperts)
                ),
                new CoefficientsOfRiskSignificanceRow(
                        new SimpleStringProperty("Множина настання планових ризикових подій"),
                        createIntProperties(numExperts)
                ), new CoefficientsOfRiskSignificanceRow(
                        new SimpleStringProperty("Множина настання ризикових подій реалізації процесу управління програмним проектом"),
                        createIntProperties(numExperts)
                ));
        coeffsOfSignificanceTable.enable();
    }

    private List<TableColumn<ExpertEvaluationIncludingCostRow, ?>> createExpertEvaluationIncludingCostTable(
            int numExperts,
            Supplier<TableView<ExpertEvaluationIncludingCostRow>> tableView
    ) {
        List<TableColumn<ExpertEvaluationIncludingCostRow, ?>> columns = new ArrayList<>();
        columns.add(createColumn("Ризик", ExpertEvaluationIncludingCostRow::riskNotation));
        for (int i = 0; i < numExperts; i++) {
            int expertIndex = i;
            columns.add(createColumn(
                    "Оцінка експеру " + (i + 1),
                    v -> v.expertsEvaluation().get(expertIndex)
            ));
        }
        columns.add(createColumn(
                "Ймовірність виникнення ризикової події",
                ExpertEvaluationIncludingCostRow::probabilityOfRiskEvent
        ));
        columns.add(
                createColumn("Класифікація настання", v -> new SimpleStringProperty(classifyRisk(v.probabilityOfRiskEvent().getValue()))));
        return columns;
    }

    private Property<Double> enrichRiskProbabilityExpertTable(int numExperts, List<RiskSourceRow> riskSourceRows) {
        int countRelevant = 0;
        List<Property<Double>> variables = new ArrayList<>();
        for (var riskSourceRow : riskSourceRows) {
            if (!Boolean.TRUE.equals(riskSourceRow.isEnabled().getValue())) {
                continue;
            }
            var expertsEval = IntStream.range(0, numExperts)
                    .boxed()
                    .map(v -> new SimpleObjectProperty<>(getRandomProbability(0.11)))
                    .toList();
            Property<Double> averageProbability = dynamic(
                    expertsEval,
                    simpleDoubleProperties -> simpleDoubleProperties.stream()
                            .map(Property::getValue)
                            .mapToDouble(v -> v).sum() / numExperts);
            var riskProbabilityExpertEvaluationRow = new RiskProbabilityExpertEvaluationRow(
                    new SimpleStringProperty(riskSourceRow.notation().getValue()),
                    expertsEval,
                    averageProbability
            );
            variables.add(averageProbability);
            riskProbabilityExpertEvaluationTable.getItems().add(riskProbabilityExpertEvaluationRow);
            countRelevant++;
        }
        int finalCountRelevant = countRelevant;
        return countRelevant == 0
                ? new SimpleObjectProperty<>(0D)
                : dynamic(variables, list -> list.stream()
                .map(ObservableValue::getValue)
                .mapToDouble(v -> v).sum() / finalCountRelevant);
    }

    private Property<Double> enrichExpertEvaluationIncludingCostTable(
            int rowIndex,
            int numExperts,
            List<RiskSourceRow> riskSourceRows
    ) {
        int index = 0;
        var coeffsSum = dynamic(coeffsOfSignificanceTable.getItems().get(rowIndex).expertCoefficients(), v -> {
            return v.stream().map(Property::getValue).mapToDouble(x -> x).sum();
        });
        List<Property<Double>> variables = new ArrayList<>();
        for (var riskSourceRow : riskSourceRows) {
            if (!Boolean.TRUE.equals(riskSourceRow.isEnabled().getValue())) {
                continue;
            }
            var riskName = riskSourceRow.notation().getValue();
            List<Property<Double>> expertsEval = IntStream.range(0, numExperts)
                    .boxed()
                    .map(v -> {
                        var coeffOfSignificance =
                                coeffsOfSignificanceTable.getItems().get(rowIndex).expertCoefficients().get(v);
                        var riskProbabilityExpertEvaluationRow =
                                riskProbabilityExpertEvaluationTable.getItems().stream()
                                        .filter(x -> x.riskNotation().get().equals(riskName))
                                        .findFirst()
                                        .orElseThrow();
                        var probability = riskProbabilityExpertEvaluationRow.expertsEvaluation().get(v);
                        return dynamic(coeffOfSignificance, probability, (a, b) -> a.doubleValue() * b);
                    })
                    .toList();
            var properties = new ArrayList<Property<Double>>();
            properties.add(coeffsSum);
            properties.addAll(expertsEval);
            var probabilityOfRiskEvent = dynamic(properties, l -> {
                double sum = expertsEval.stream().map(Property::getValue).mapToDouble(v -> v).sum();
                return sum / coeffsSum.getValue();
            });
            var expertEvaluationIncludingCostRow = new ExpertEvaluationIncludingCostRow(
                    new SimpleStringProperty(riskName),
                    expertsEval,
                    probabilityOfRiskEvent
            );
            variables.add(probabilityOfRiskEvent);
            expertEvaluationIncludingCostTable.getItems().add(expertEvaluationIncludingCostRow);
            index++;
        }
        int finalCountRelevant = index;
        return finalCountRelevant == 0
                ? new SimpleObjectProperty<>(0D)
                : dynamic(variables, list -> list.stream().map(ObservableValue::getValue).mapToDouble(v -> v).sum() / finalCountRelevant);
    }

    private List<? extends Property<Integer>> createIntProperties(int n) {
        return IntStream.range(0, n)
                .boxed()
                .map(v -> new SimpleObjectProperty<>(getRandomInt()))
                .toList();
    }

    private List<TableColumn<CoefficientsOfRiskSignificanceRow, ?>> createCoeffsOfSignificanceTableColumns(int numExperts, Supplier<TableView<CoefficientsOfRiskSignificanceRow>> tableView) {
        List<TableColumn<CoefficientsOfRiskSignificanceRow, ?>> columns = new ArrayList<>();
        columns.add(createColumn("Тип", CoefficientsOfRiskSignificanceRow::riskType));
        for (int i = 0; i < numExperts; i++) {
            int expertIndex = i;
            columns.add(createEditableDoubleColumn(
                    "Оцінка від експерта " + (i + 1),
                    v -> v.expertCoefficients().get(expertIndex),
                    tableView,
                    new StringConverter<>() {
                        @Override
                        public String toString(Integer object) {
                            return object == null ? null : object.toString();
                        }

                        @Override
                        public Integer fromString(String string) {
                            if (string != null && string.matches(DOUBLE_REGEX)) {
                                return Integer.parseInt(string);
                            }
                            return null;
                        }
                    }
            ));
        }
        return columns;
    }

    // return sum
    private Property<Double> enrichLossesRisksTable(int numExperts, List<RiskSourceRow> riskSourceRows) {
        List<Property<Double>> vars = new ArrayList<>();
        for (var riskSourceRow : riskSourceRows) {
            if (!Boolean.TRUE.equals(riskSourceRow.isEnabled().getValue())) {
                continue;
            }
            var riskName = riskSourceRow.notation().getValue();
            var proportionOfPossibleLoss = IntStream.range(0, numExperts)
                    .boxed()
                    .map(v -> new SimpleObjectProperty<>(getRandomProbability(0.22)))
                    .toList();
            var lossesAmountFromRiskOccurrence = IntStream.range(0, numExperts)
                    .boxed()
                    .map(v -> {
                        var evaluationRow = riskProbabilityExpertEvaluationTable.getItems().stream()
                                .filter(x -> x.riskNotation().get().equals(riskName))
                                .findFirst()
                                .orElseThrow();
                        return dynamic(proportionOfPossibleLoss.get(v), evaluationRow.expertsEvaluation().get(v), (a, b) -> a * b);
                    })
                    .toList();
            var riskInitialCost = new SimpleObjectProperty<>(getRandomExpectedInitialCost());
            List<Property<Double>> variables = new ArrayList<>(proportionOfPossibleLoss);
            variables.add(riskInitialCost);
            var averageExpectedCost = dynamic(
                    variables,
                    list -> proportionOfPossibleLoss.stream().mapToDouble(Property::getValue).sum()
                            / proportionOfPossibleLoss.size()
                            * riskInitialCost.get()
            );
            var riskProbabilityExpertEvaluationRow = new LossesRiskRow(
                    new SimpleStringProperty(riskName),
                    riskInitialCost,
                    lossesAmountFromRiskOccurrence,
                    averageExpectedCost
            );
            lossesRisksTable.getItems().add(riskProbabilityExpertEvaluationRow);
            vars.add(riskInitialCost);
        }
        return dynamic(vars, l -> l.stream().mapToDouble(ObservableValue::getValue).sum());
    }

    private double getRandomExpectedInitialCost() {
        return 30D + new Random().nextDouble(50);
    }

    private void calculatePriorities() {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (var lossExpertEvaluationIncludingCostRow : lossExpertEvaluationIncludingCostTable.getItems()) {
            var additionalCost = lossExpertEvaluationIncludingCostRow.additionalCost();
            min = Math.min(min, additionalCost.getValue());
            max = Math.max(max, additionalCost.getValue());
        }
        double mpr = (max - min) / 3;
        for (var lossExpertEvaluationIncludingCostRow : lossExpertEvaluationIncludingCostTable.getItems()) {
            var h = lossExpertEvaluationIncludingCostRow.additionalCost().getValue();
            if (h >= max - mpr) {
                lossExpertEvaluationIncludingCostRow.priority().setValue("Високий");
            } else if (h >= max - (mpr * 2)) {
                lossExpertEvaluationIncludingCostRow.priority().setValue("Середній");
            } else {
                lossExpertEvaluationIncludingCostRow.priority().setValue("Низький");
            }
        }
    }

    private double highest(List<? extends DoubleProperty> list) {
        return list.stream().map(DoubleExpression::getValue).mapToDouble(v -> v).max().orElseThrow();
    }

    private <A, B, C> Property<C> dynamic(Property<A> a, Property<B> b, BiFunction<A, B, C> func) {
        Property<C> res = new SimpleObjectProperty<>(func.apply(a.getValue(), b.getValue()));
        a.addListener((observable, oldValue, newValue) ->
                res.setValue(func.apply(newValue, b.getValue())));
        b.addListener((observable, oldValue, newValue) ->
                res.setValue(func.apply(a.getValue(), newValue)));
        return res;
    }
    private <T, U, R extends Property<T>> Property<U> dynamic(List<R> properties, Function<List<R>, U> func) {
        Property<U> property = new SimpleObjectProperty<>();
        property.setValue(func.apply(properties));
        for (Property<T> dependency : properties) {
            dependency.addListener((observable, oldValue, newValue) -> property.setValue(func.apply(properties)));
        }
        return property;
    }

    private String classifyRisk(double p) {
        if (p < 0.1) {
            return "Дуже низька";
        } else if (p < 0.25) {
            return "Низька";
        } else if (p < 0.5) {
            return "Середня";
        } else if (p < 0.75) {
            return "Висока";
        }
        return "Дуже висока";
    }

    private double getRandomProbability(double low) {
        return low + new SecureRandom().nextDouble(1 - low);
    }

    private int getRandomInt() {
        return new SecureRandom().nextInt(10) + 1;
    }

    private Node namedInput(Node input, String label) {
        var hBox = new GridPane();
        var labelObj = new Label(label);
        labelObj.setFont(INPUT_FONT);
        hBox.add(labelObj, 0, 0);
        hBox.add(input, 1, 0);
        return hBox;
    }

    private List<TableColumn<LossesRiskRow, ?>> createLossesRiskTableColumns(Supplier<TableView<LossesRiskRow>> tableView) {
        var numOfExperts = getNumberOfExperts();
        List<TableColumn<LossesRiskRow, ?>> columns = new ArrayList<>();
        columns.add(createColumn("Ризик", LossesRiskRow::riskNotation));
        columns.add(createColumn("Початкова вартість", LossesRiskRow::riskInitialCost));
        for (int i = 0; i < numOfExperts; i++) {
            int expertIndex = i;
            columns.add(createColumn(
                    "Оцінка від експерту " + (i + 1),
                    v -> v.proportionOfPossibleLoss().get(expertIndex)));
        }
//        for (var i = 0; i < numOfExperts; i++) {
//            int expertIndex = i;
//            columns.add(createColumn(
//                    "Величина ризику за оцінкою експерта " + (i + 1),
//                    v -> v.lossAmountFromRiskOccurrence().get(expertIndex)));
//        }
//        columns.add(createColumn("Пріоритет", LossesRiskRow::riskPriority));

        columns.add(createColumn("Середня очікувана вартість", LossesRiskRow::averageExpectedCost));

        return columns;
    }


    private List<TableColumn<RiskProbabilityExpertEvaluationRow, ?>> createRiskProbabilityExpertEvaluationTableColumns(
            int numExperts,
            Supplier<TableView<RiskProbabilityExpertEvaluationRow>> tableView
    ) {
        List<TableColumn<RiskProbabilityExpertEvaluationRow, ?>> columns = new ArrayList<>();
        columns.add(createColumn("Ризик", RiskProbabilityExpertEvaluationRow::riskNotation));
        for (int i = 0; i < numExperts; i++) {
            int expertIndex = i;
            columns.add(createEditableDoubleColumn(
                    "Оцінка експеру " + (i + 1),
                    v -> v.expertsEvaluation().get(expertIndex),
                    tableView,
                    new StringConverter<>() {
                        @Override
                        public String toString(Double object) {
                            return object == null ? null : object.toString();
                        }

                        @Override
                        public Double fromString(String string) {
                            if (string != null && string.matches(DOUBLE_REGEX)) {
                                return Double.parseDouble(string);
                            }
                            return null;
                        }
                    }
            ));
        }
        columns.add(createColumn("ER_p", RiskProbabilityExpertEvaluationRow::erp));
        columns.add(createColumn("Класифікація настання", v -> new SimpleStringProperty(classifyRisk(v.erp().getValue().doubleValue()))));
        return columns;
    }

    private int getNumberOfExperts() {
        return Integer.parseInt(numExperts.getText());
    }

    private List<TableColumn<RiskSourceRow, ?>> createRiskSourcesColumns(Supplier<TableView<RiskSourceRow>> tableView) {
        return List.of(
                createColumn("Позначення", RiskSourceRow::notation),
                createEditableColumn("Опис", RiskSourceRow::description, tableView, new StringConverter<>() {
                    @Override
                    public String toString(String object) {
                        return object;
                    }

                    @Override
                    public String fromString(String string) {
                        return string;
                    }
                }),
                createFlagColumn("Врахований", RiskSourceRow::isEnabled)
        );
    }

    private void fillTables() {
        technicalRiskSourcesTable.getItems().addAll(
                createInitialSourcesRow("t(r)1", "затримки у постачанні обладнання, необхідного для підтримки процесу розроблення ПЗ"),
                createInitialSourcesRow("t(r)2", "затримки у постачанні інструментальних засобів, необхідних для підтримки процесу розроблення ПЗ"),
                createInitialSourcesRow("t(r)3", "небажання команди виконавців ПЗ використовувати інструментальні засоби для підтримки процесу розроблення ПЗ"),
                createInitialSourcesRow("t(r)4", "відмова команди виконавців від CASE-засобів розроблення ПЗ"),
                createInitialSourcesRow("t(r)5", "формування запитів на більш потужні інструментальні засоби розроблення ПЗ"),
                createInitialSourcesRow("t(r)6", "недостатня продуктивність баз(и) даних для підтримки процесу розроблення ПЗ"),
                createInitialSourcesRow("t(r)7", "програмні компоненти, які використовують повторно в ПЗ, мають дефекти та обмежені функціональні можливості"),
                createInitialSourcesRow("t(r)8", "неефективність програмного коду, згенерованого CASE-засобами розроблення ПЗ"),
                createInitialSourcesRow("t(r)9", "неможливість інтеграції CASE-засобів з іншими інструментальними засобами для підтримки процесу розроблення ПЗ"),
                createInitialSourcesRow("t(r)10", "швидкість виявлення дефектів у програмному коді є нижчою від раніше запланованих термінів"),
                createInitialSourcesRow("t(r)11", "поява дефектних системних компонент, які використовують для розроблення ПЗ")
        );

        costRiskSourcesTable.getItems().addAll(
                createInitialSourcesRow("c(r)1", "недооцінювання витрат на реалізацію програмного проекту (надмірно низька вартість)"),
                createInitialSourcesRow("c(r)2", "переоцінювання витрат на реалізацію програмного проекту (надмірно висока вартість)"),
                createInitialSourcesRow("c(r)3", "фінансові ускладнення у компанії-замовника ПЗ"),
                createInitialSourcesRow("c(r)4", "фінансові ускладнення у компанії-розробника ПЗ"),
                createInitialSourcesRow("c(r)5", "збільшення бюджету програмного проекта з ініціативи компанії-розробника ПЗ під час його реалізації"),
                createInitialSourcesRow("c(r)6", "збільшення бюджету програмного проекта з ініціативи компанії-розробника ПЗ під час його реалізації"),
                createInitialSourcesRow("c(r)7", "висока вартість виконання повторних робіт, необхідних для зміни вимог до ПЗ"),
                createInitialSourcesRow("c(r)8", "реорганізація структурних підрозділів у компанії-замовника ПЗ"),
                createInitialSourcesRow("c(r)9", "реорганізація команди виконавців у компанії-розробника ПЗ")
        );

        planningRiskSourcesTable.getItems().addAll(
                createInitialSourcesRow("p(r)1", "зміни графіка виконання робіт з боку замовника чи виконавця"),
                createInitialSourcesRow("p(r)2", "порушення графіка виконання робіт у компанії-розробника ПЗ"),
                createInitialSourcesRow("p(r)3", "потреба зміни користувацьких вимог до ПЗ з боку компанії-замовника ПЗ"),
                createInitialSourcesRow("p(r)4", "потреба зміни функціональних вимог до ПЗ з боку компанії-розробника ПЗ"),
                createInitialSourcesRow("p(r)5", "потреба виконання великої кількості повторних робіт, необхідних для зміни вимог до ПЗ"),
                createInitialSourcesRow("p(r)6", "недооцінювання тривалості етапів реалізації програмного проекту з боку компанії - розробника ПЗ"),
                createInitialSourcesRow("p(r)7", "переоцінювання тривалості етапів реалізації програмного проекту"),
                createInitialSourcesRow("p(r)8", "остаточний розмір ПЗ перевищує заплановані його характеристики"),
                createInitialSourcesRow("p(r)9", "остаточний розмір ПЗ значно менший за планові його характеристики"),
                createInitialSourcesRow("p(r)10", "поява на ринку аналогічного ПЗ до виходу замовленого"),
                createInitialSourcesRow("p(r)11", "поява на ринку більш конкурентоздатного ПЗ")
        );

        managingRiskSourcesTable.getItems().addAll(
                createInitialSourcesRow("m(r)1", "низький моральний стан персоналу команди виконавців ПЗ"),
                createInitialSourcesRow("m(r)2", "низька взаємодія між членами команди виконавців ПЗ"),
                createInitialSourcesRow("m(r)3", "пасивність керівника (менеджера) програмного проекту"),
                createInitialSourcesRow("m(r)4", "недостатня компетентність керівника (менеджера) програмного проекту"),
                createInitialSourcesRow("m(r)5", "незадоволеність замовника результатами етапів реалізації програмного проекту"),
                createInitialSourcesRow("m(r)6", "недостатня кількість фахівців у команді виконавців ПЗ з необхідним професійним рівнем"),
                createInitialSourcesRow("m(r)7", "хвороба провідного виконавця в найкритичніший момент розроблення ПЗ"),
                createInitialSourcesRow("m(r)8", "одночасна хвороба декількох виконавців підчас розроблення ПЗ"),
                createInitialSourcesRow("m(r)9", "неможливість організації необхідного навчання персоналу команди виконавців ПЗ"),
                createInitialSourcesRow("m(r)10", "зміна пріоритетів у процесі управління програмним проектом"),
                createInitialSourcesRow("m(r)11", "недооцінювання необхідної кількості розробників (підрядників і субпідрядників) на етапах життєвого циклу розроблення ПЗ"),
                createInitialSourcesRow("m(r)12", "переоцінювання необхідної кількості розробників (підрядників і субпідрядників) на етапах життєвого циклу розроблення ПЗ"),
                createInitialSourcesRow("m(r)13", "надмірне документування результатів на етапах реалізації програмного проекту"),
                createInitialSourcesRow("m(r)14", "недостатнє документування результатів на етапах реалізації програмного проекту"),
                createInitialSourcesRow("m(r)15", "нереалістичне прогнозування результатів на етапах реалізації програмного проекту"),
                createInitialSourcesRow("m(r)16", "недостатній професійний рівень представників від компанії-замовника ПЗ")
        );

    }

    private RiskSourceRow createInitialSourcesRow(String notation, String description) {
        return new RiskSourceRow(new SimpleStringProperty(notation), new SimpleStringProperty(description), new SimpleBooleanProperty(true));
    }

}

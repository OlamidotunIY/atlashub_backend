package com.atlashub.shared.domain.valueobject;

public class Anchor {

    public enum CustomerType {
        IndividualCustomer, BusinessCustomer
    }

    public enum BusinessRegistrationType {
        Private_Incorporated, Incorporated_Trustees, Business_Name, Free_Zone, Gov, Private_Incorporated_Gov, Cooperative_Society, Public_Incorporated
    }

    public enum Industry {
        Agriculture_AgriculturalCooperatives,
        Agriculture_AgriculturalServices,
        Commerce_Automobiles,
        Commerce_DigitalGoods,
        Commerce_PhysicalGoods,
        Commerce_RealEstate,
        Commerce_DigitalServices,
        Commerce_LegalServices,
        Commerce_PhysicalServices,
        Commerce_ProfessionalServices,
        Commerce_OtherProfessionalServices,
        Education_NurserySchools,
        Education_PrimarySchools,
        Education_SecondarySchools,
        Education_TertiaryInstitutions,
        Education_VocationalTraining,
        Education_VirtualLearning,
        Education_OtherEducationalServices,
        Gaming_Betting,
        Gaming_Lotteries,
        Gaming_PredictionServices,
        FinancialServices_FinancialCooperatives,
        FinancialServices_CorporateServices,
        FinancialServices_PaymentSolutionServiceProviders,
        FinancialServices_Insurance,
        FinancialServices_Investments,
        FinancialServices_AgriculturalInvestments,
        FinancialServices_Lending,
        FinancialServices_BillPayments,
        FinancialServices_Payroll,
        FinancialServices_Remittances,
        FinancialServices_Savings,
        FinancialServices_MobileWallets,
        Health_Gyms,
        Health_Hospitals,
        Health_Pharmacies,
        Health_HerbalMedicine,
        Health_Telemedicine,
        Health_MedicalLaboratories,
        Hospitality_Hotels,
        Hospitality_Restaurants,
        Nonprofits_ProfessionalAssociations,
        Nonprofits_GovernmentAgencies,
        Nonprofits_NGOs,
        Nonprofits_PoliticalParties,
        Nonprofits_ReligiousOrganizations,
        Nonprofits_Leisure_Entertainment,
        Nonprofits_Cinemas,
        Nonprofits_Nightclubs,
        Nonprofits_Events,
        Nonprofits_Press_Media,
        Nonprofits_RecreationCentres,
        Nonprofits_StreamingServices,
        Logistics_CourierServices,
        Logistics_FreightServices,
        Travel_Airlines,
        Travel_Ridesharing,
        Travel_TourServices,
        Travel_Transportation,
        Travel_TravelAgencies,
        Utilities_CableTelevision,
        Utilities_Electricity,
        Utilities_Garbage_Disposal,
        Utilities_Internet,
        Utilities_Telecoms,
        Utilities_Water,
        Retail,
        Wholesale,
        Restaurants,
        Construction,
        Unions,
        RealEstate,
        FreelanceProfessional,
        OtherProfessionalServices,
        OtherEducationServices
    }

    public enum BusinessRole {
        OWNER, DIRECTOR
    }

    public record ReserveAccountResponse(
            String accountId,
            Bank bank,
            String accountName,
            String accountNumber
    ) {
        public record Bank(
                String provider,
                String name
        ) {
        }
    }
}
